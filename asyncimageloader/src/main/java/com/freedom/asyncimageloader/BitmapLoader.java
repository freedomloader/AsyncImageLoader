/**
 * Copyright 2014 Freedom-Loader Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.freedom.asyncimageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.freedom.asyncimageloader.interfaces.PhotoLoadFrom;
import com.freedom.asyncimageloader.assist.Decoder;
import com.freedom.asyncimageloader.assist.Resizer;
import com.freedom.asyncimageloader.assist.BitmapDecoder.RequestForDecode;
import com.freedom.asyncimageloader.download.Downloader;
import com.freedom.asyncimageloader.listener.CusListener;
import com.freedom.asyncimageloader.uri.URLEncoded.UriScheme;
import com.freedom.asyncimageloader.utils.Logg;
import com.freedom.asyncimageloader.utils.Utility;

public class BitmapLoader implements CusListener.failedListener {

	private static final int IO_BUFFER_SIZE = 32 * 1024;
	Bitmap bitmap = null;
	final LoaderOptions options;
	final RequestData data;
	final Context context;
	final Decoder imageDecoder;
	final Downloader downloader;
	final LoaderManager manager;
	PhotoLoadFrom loadFrom;

	public BitmapLoader(LoaderOptions options, LoaderManager manager,
			RequestData data) {
		this.options = options;
		this.context = options.context;
		this.data = data;
		this.manager = manager;
		this.imageDecoder = options.imageDecoder;
		this.downloader = options.downloader;
	}

	public Bitmap load(String uri)  {
	    bitmap = options.memoryCache.get(data.getMemoryCacheKey());
	    if (bitmap != null && !data.setting.skipMemoryCache) {
	    	return bitmap;
		}
	    return load(Uri.parse(uri));
	}
	 
	public Bitmap load(Uri uri) {
		final String uriName = uri.toString();
		final File f = getDiscCacheFile(uriName);
		String fileKeyUri = UriScheme.FILE.drag(f != null ? f.getAbsolutePath() : "");

		try {
			if (f.exists()) {
				bitmap = decodeFile(fileKeyUri, data.targetSize, false);
			}

			if (bitmap != null) {
				options.memoryCache.put(data.getMemoryCacheKey(), bitmap);
				loadFrom = PhotoLoadFrom.DISC;
			} else {
				if (manager.networkUri(uriName) == true) {
					bitmap = get(uriName, f);
				} else {
					bitmap = cahceLoad(uriName, f);
				}
				loadFrom = PhotoLoadFrom.NETWORK;
			}
		} catch (Exception e1) {
	        manager.delete(f);
		}
		return bitmap;
	}

	Bitmap get(final String uriName, final File f) {
		if (manager.networkUri(uriName) == true) {
			options.fatchExecutor.execute(new Runnable() {
				@Override
				public void run() {
					if (imageViewReused())
						return;
					try {
						bitmap = cahceLoad(uriName, f);
					} catch (IOException e) {
					}
				}
			});
		}
		return bitmap;
	}

	private Bitmap cahceLoad(String uriName, File cacheFile) throws IOException {
		Bitmap bitmap = null;
		String fileKeyUri = UriScheme.FILE.drag(cacheFile.getAbsolutePath());
		InputStream downloadStream = null;
		OutputStream output = null;

		if (uriName == null) {
			return null;
		}
		if (uriName.length() == 0) {
			return null;
		}
		/*if (manager.networkUri(uriName) == false) {
			downloadStream = downloader.downloadStream(data.getUriRequest(),this);
		} else {
			throw new IllegalStateException("Network uri is not allow uri: "
			 + uriName);
		}*/
		downloadStream = downloader.downloadStream(data.getUriRequest(),this);
		try {
			output = new BufferedOutputStream(new FileOutputStream(cacheFile),
					IO_BUFFER_SIZE);
			Logg.debug("writing bytes to file from - " + uriName);
			Utility.CopyStream(downloadStream, output);
		} finally {
			Utility.closeQuietly(output);
			Utility.closeQuietly(downloadStream);
		}

		if (cacheFile.exists()) {
			bitmap = decodeFile(fileKeyUri, data.targetSize, true);
		}

		if (bitmap != null) {
			options.memoryCache.put(data.getMemoryCacheKey(), bitmap);
		}
		return bitmap;
	}

	private File getDiscCacheFile(String uri) {
  	    File imageFile = null;
  	    if (options.discCache == null) {
  		    return null;
  	    }
  	    
  	    if (data.setting.shouldCahceThumbnailOnDisc()) {
  	  	    final String extensionName  = "-" + data.size.width + "x" + data.size.height+options.discCahceFileNameExtension;
  	        imageFile = options.discCache.getFile(uri,extensionName);
  	    }
  	    if (imageFile == null || !imageFile.exists()) {
  	        imageFile = options.discCache.getFile(uri,options.discCahceFileNameExtension);
  	        if (data.setting.shouldCahceThumbnailOnDisc()) {
  	        	log_i("image thumbnail on disc cache is allow");
  	        }
  	     }
  	     if (imageFile == null || !imageFile.exists()) {
		     File cacheDir = imageFile.getParentFile();
		     if (cacheDir == null || (!cacheDir.exists() && !cacheDir.mkdirs())) {
			     cacheDir = options.discCache.getStorage();
			     if (cacheDir != null && !cacheDir.exists()) {
				     cacheDir.mkdirs();
			     }
			     if (cacheDir != null) {
			    	 options.discCache.init(cacheDir, options.discFileNameGenerator);
		  	         imageFile = options.discCache.getFile(uri,options.discCahceFileNameExtension);
			     }
		     }
	     }
  	  	 return imageFile;// find image in disc cache
    }
	
	/**
	 * decode download file
	 * 
	 * @return bitmap
	 */
	private Bitmap decodeFile(String key, Resizer targetSize,
			boolean useDownloaderForInputStream) throws IOException {
		RequestForDecode decodRequest = new RequestForDecode(key,
				useDownloaderForInputStream, true, targetSize, targetSize,
				manager.createBitmapOptions(data), data);
		return imageDecoder.decodeFile(decodRequest);
	}

	private boolean imageViewReused() {
		String loadingCacheKey = manager.requestUriView(data.getImageWrapper());
		boolean isImageWrapperReused = loadingCacheKey == null
				|| !data.getMemoryCacheKey().equals(loadingCacheKey);
		if (isImageWrapperReused) {
			return true;
		}
		return false;
	}

	public PhotoLoadFrom getLoadedFrom() {
		return loadFrom;
	}

	@Override
	public boolean onFailed(String url) {
		// TODO Auto-generated method stub
		return false;
	}
	
	private void log_i(String message) {
		if (options.isLoggingEnabled()) Logg.info(message);
	}
}
