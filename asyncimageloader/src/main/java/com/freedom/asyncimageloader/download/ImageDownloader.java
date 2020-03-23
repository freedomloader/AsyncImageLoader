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
package com.freedom.asyncimageloader.download;

import static android.content.ContentResolver.SCHEME_ANDROID_RESOURCE;
import static android.content.ContentResolver.SCHEME_CONTENT;
import static android.content.ContentResolver.SCHEME_FILE;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.freedom.asyncimageloader.assist.ImageStreamLoader;
import com.freedom.asyncimageloader.assist.ImageStreamLoader.StreamCallback;
import com.freedom.asyncimageloader.listener.CusListener;
import com.freedom.asyncimageloader.uri.UriRequest;
import com.freedom.asyncimageloader.uri.UriSubmake;
import com.freedom.asyncimageloader.uri.URLEncoded.UriScheme;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.ContactsContract.Contacts;
import com.freedom.asyncimageloader.*;

public class ImageDownloader implements Downloader {

	private static final String CONTACT_START_CONTENT = "content://com.android.contacts/";
	protected static final String ANDROID_ASSET = "android_asset";
	static final int ASSET_PREFIX_LENGTH = (SCHEME_FILE + ":///" + ANDROID_ASSET + "/").length();
	
	final ImageStreamLoader streamLoader;
	private LoaderManager manager;
	final Context context;
	Handler handler = new Handler();
	
	public ImageDownloader(Context context,ImageStreamLoader streamLoader) {
		this.streamLoader = streamLoader;
		this.context = context;
	}

	@Override
	public void setManager(LoaderManager manager) {
		this.manager = manager;
	}
	
	@Override
	public void loadStream(final UriRequest uridata,StreamCallback streamCallback) {
		try {
		  InputStream loadedStream = downloadStream(uridata,null);
		  streamCallback.onStreamLoaded(new Response(loadedStream,loadedStream.available()));
	    } catch (IOException e) {
	      streamCallback.onException(e);
	    }
	}
	
	@Override
	public Response downloadStream(final UriRequest uridata) throws IOException {
		  InputStream loadedStream = downloadStream(uridata,null);
		  return new Response(loadedStream,loadedStream.available());
	}
	
	@Override
	public InputStream downloadStream(final UriRequest uridata,
			CusListener.failedListener listener) throws IOException {
		
			InputStream stream = null;
		    boolean shouldLoadFromDrawable = uridata.getUriDrawable() != 0;
		    if (shouldLoadFromDrawable) {
			     return streamLoader.getImageStreamDrawable(uridata.getUriDrawable());
		    }
		    Object otherUri = uridata.image;
		    boolean otherUri3 = otherUri != null;
		    if (otherUri3) {
			    if (otherUri instanceof Bitmap) 
				     return streamLoader.bitmapToInputStream((Bitmap) otherUri);
			    else if (otherUri instanceof Drawable) 
				     return streamLoader.drawableToInputStream((Drawable) otherUri);
			    else if (otherUri instanceof Uri) 
				     return streamLoader.getContactsStream(((Uri) otherUri));
			    else if (otherUri instanceof Integer) 
				     return streamLoader.getImageStreamDrawable(((Integer) otherUri));
			    else if (otherUri instanceof File) 
				     return streamLoader.getImageStreamStorage(((File) otherUri).getAbsolutePath());
			    else if (otherUri instanceof InputStream) 
			         return (InputStream) otherUri;
		      }
		      final Uri uri = uridata.getUri();
		      String scheme = uri.getScheme();
		
		      if (SCHEME_FILE.equals(scheme) && !UriSubmake.isThumbFormat(uri.toString())) {
			      if (!uri.getPathSegments().isEmpty()
					&& ANDROID_ASSET.equals(uri.getPathSegments().get(0))) {
				      return streamLoader.getImageStreamAssets(uri.toString());
			       }
			      return streamLoader.getImageStreamContent(uri.toString());
		      } else if (SCHEME_ANDROID_RESOURCE.equals(scheme)) {
			      return streamLoader.getImageStreamDrawable(
					Integer.valueOf(uri.toString()));
		      } else if (uri.toString().startsWith(CONTACT_START_CONTENT)) {
				  return streamLoader.getContactsStream(uri);
		      } else {
				  switch (uridata.getSchemeType()) {
					  case HTTP:
					  case HTTPS:
						  return streamLoader.getConnectionStream(uri.toString(),
								  listener);
					  case FILE:
                      case FILE_S:
						  return streamLoader.getImageStreamStorage(
								  UriScheme.FILE.remove(uri.toString()));
					  case CONTENT:
						  return streamLoader.getImageStreamContent(
								  UriScheme.CONTENT.remove(uri.toString()));
					  case ASSETS:
						  return streamLoader.getImageStreamAssets(
								  UriScheme.ASSETS.remove(uri.toString()));
					  case RAW:
						  return streamLoader.getImageStreamRaw(
								  UriScheme.RAW.remove(uri.toString()));
					  case THUMB:
						  String thumburi = UriScheme.THUMB.remove(uri.toString());
						  return streamLoader.getImageStreamVideoThumb(
								  UriScheme.FILE.remove(thumburi));
					  case DRAWABLE:
						  return streamLoader.getImageStreamDrawable(
								  Integer.parseInt(UriScheme.DRAWABLE.remove(uri.toString())));
					  case UNKNOWN_URI:
						  if (SCHEME_CONTENT.equals(scheme)) {
							  if (Contacts.CONTENT_URI.getHost().equals(uri.getHost()) &&
									  !uri.getPathSegments().contains(Contacts.Photo.CONTENT_DIRECTORY)) {
								  return streamLoader.getContactsStream(uri);
							  } else if (MediaStore.AUTHORITY.equals(uri.getAuthority())) {
								  return streamLoader.getContactsStream(uri);
							  } else {
								  return streamLoader.getImageStreamContent(uri.toString());
							  }
						  }
						  // break;
					  default:
						  throw new IllegalStateException("Unknown uri: " + uri.toString());
				  }
	     	}
	}
	
  /**
   * Returns <tt>true</tt> - if images should be loaded contact
   */
   boolean isUriContactPhoto(Uri uri) {
	   boolean result = uri.toString().startsWith(CONTACT_START_CONTENT);
	   return result ;
   }
}
