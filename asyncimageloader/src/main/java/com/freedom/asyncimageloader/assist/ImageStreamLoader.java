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
package com.freedom.asyncimageloader.assist;

import java.io.IOException;
import java.io.InputStream;

import com.freedom.asyncimageloader.download.Downloader.Response;
import com.freedom.asyncimageloader.interfaces.HttpSocket;
import com.freedom.asyncimageloader.listener.CusListener;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;

public interface ImageStreamLoader {
	boolean bitmapCompressFormat(Bitmap.CompressFormat format);
	InputStream getConnectionStream(String url) throws IOException;
	InputStream getConnectionStream(String uri,CusListener.failedListener listener) throws IOException;
	InputStream getConnectionStream(HttpSocket socket,String uri,CusListener.failedListener listener) throws IOException;
	InputStream getImageStreamDrawable(int drawableId);
	InputStream getImageStreamAssets(String imageUrl) throws IOException;
	InputStream getImageStreamStorage(String imageUrl) throws IOException;
	InputStream getImageStreamContent(String imageUrl) throws IOException;
	InputStream getImageStreamRaw(String filePath) throws IOException;
	InputStream drawableToInputStream(Drawable d) throws IOException;
	InputStream getImageStreamVideoThumb(String mMediaPath) throws IOException;
	InputStream bitmapToInputStream(Bitmap bitmap) throws IOException;
	InputStream getContactsStream(Uri uri) throws IOException;

	public static interface StreamCallback {
		void onStreamLoaded(Response response);
		void onException(Throwable e);
	}
}
