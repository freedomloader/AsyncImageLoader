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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import android.graphics.Bitmap;

import com.freedom.asyncimageloader.assist.Decoder;
import com.freedom.asyncimageloader.download.Downloader;
import com.freedom.asyncimageloader.listener.LoadingListener;
import com.freedom.asyncimageloader.network.NetworkManager;
import com.freedom.asyncimageloader.cache.*;
import com.freedom.asyncimageloader.utils.*;
import android.widget.*;

public class ImageLoaderMonitor {
	private final LoaderManager manager;
	private final LoaderOptions options;

	public ImageLoaderMonitor(LoaderOptions options, LoaderManager manager) {
		this.options = options;
		this.manager = manager;
	}

	public Executor getLoadingExecutor() {
		return manager.getLoadingTask();
	}

	public Decoder getDecoder() {
		return options.imageDecoder;
	}

	public Downloader getDownloader() {
		return options.downloader;
	}

	public void clean() {
		options.memoryCache.clear();
		if (options.discCache != null) {
			options.discCache.clear();
		}
	}

	public void cleanOlderDiscCache(int mills) {
		if (options.discCache != null) {
			options.discCache.cleanCache(options.context, mills);
		}
	}

	public Bitmap getMemoryBitmap(String uri, int width, int height) {
		String cachename = uri + "-" + width + "x" + height;
		if (options.memoryCache != null) {
			return options.memoryCache.get(cachename);
		}
		return null;
	}
	
	public Bitmap getMemoryBitmap(String key) {	
	    return getMemoryBitmap(key,false);
	}
	
	public Bitmap getMemoryBitmap(String key,boolean isUseFindMemCacheAsBitmap) {	
		if (options.memoryCache != null) {
			boolean isCacheHolderFound = false;
			if (isUseFindMemCacheAsBitmap) {
				StringBuffer builder = new StringBuffer();
	
				builder.append(key);
				builder.append('\n');
				builder.append(FileKeyGenerator. FIND_CACHE_KEY);
					
				String memoryCacheKey = builder.toString();
				String incomingCacheKey = memoryCacheKey;
				List<String> cacheRequest = options.memoryCache.retrieveListOfCacheKey();

				for (String cacheKey : cacheRequest) {
					if (!cacheKey.contains(AsyncImage.FREEDOM_MARGE_BITMAPS_URI)) {
						String normalSaveCacheKey = getFirstWord(cacheKey,FileKeyGenerator.FIND_CACHE_KEY);
						String incomingSaveCacheKey = getFirstWord(incomingCacheKey,FileKeyGenerator.FIND_CACHE_KEY);
						boolean isFound = normalSaveCacheKey.equals(incomingSaveCacheKey);
					
						if (isFound) {
							isCacheHolderFound = true;
							Bitmap bit = options.memoryCache.get(cacheKey);
							return bit;
						}
					}
				}
			} else {
				Bitmap bit = options.memoryCache.get(key);
				return bit;
			}
		}
		return null;
	}
	
	public String getFirstWord(String uri,String isIndex) {
		if (isIndex != null && isIndex.length() == 0)
			return "";

		if (isIndex != null && isIndex.length() != 0) {
			int i = uri.indexOf(isIndex);
			if (i > 0) {
				return uri.substring(0, i);
			}
		}
		return uri;
	}

	private String getLastWord(String uri,String isIndex) {
		if (isIndex != null && isIndex.length() == 0) 
			return uri;

		if (isIndex != null && isIndex.length() != 0) {
			int i = uri.lastIndexOf(isIndex);
			if (i > 0) {
				return uri.substring(i, uri.length());
			}
		}
		return uri;
	}
	
	public File getDiscFile(String uri) {
		if (options.discCache != null) {
			return options.discCache.getFile(uri);
		}
		return null;
	}

	public File getDiscFile(String uri, int width, int height) {
		String cachename = uri + "-" + width + "x" + height;
		if (options.discCache != null) {
			return options.discCache.getFile(cachename);
		}
		return null;
	}

	public void removeKeyInMemory(String uri) {
		List<String> requestRemove = retrieveListOfCacheUri(uri);
		for (String removeKeys : requestRemove) {
			options.memoryCache.remove(removeKeys);
		}
	}
	
	public void registerLoadedListener(LoadingListener callback) {
		manager.getWorkerCallback().multiCallback.registerLoaderListener(callback);
    }
	
	public void unRegisterLoadedListener(LoadingListener callback) {
        manager.getWorkerCallback().multiCallback.unRegisterLoaderListener(callback);
    }
	
	public NetworkManager getNetworkManager() {
		return options.networkManager;
	}

	public List<String> retrieveListOfCacheUri(String uri) {
		return options.memoryCache.retrieveListOfCacheKey();
	}
	
	public String getMargeBitmapKeyWithPixel(String key, int pixel) {
		return manager.getMargeBitmapKeyWithPixel(key,pixel);
	}

	public boolean putMargeBitmapPixelWithKey(String key, int pixel, String pixelKey) {
		return manager.putMargeBitmapPixelWithKey(key,pixel,pixelKey);
	}

	public boolean removeMargeBitmapPixelWithKey(String key) {
		return manager.removeMargeBitmapPixelWithKey(key);
	}
}
