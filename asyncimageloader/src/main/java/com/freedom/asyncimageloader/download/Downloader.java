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

import java.io.IOException;
import java.io.InputStream;

import com.freedom.asyncimageloader.assist.ImageStreamLoader.StreamCallback;
import com.freedom.asyncimageloader.listener.CusListener;
import com.freedom.asyncimageloader.uri.UriRequest;
import com.freedom.asyncimageloader.*;

public interface Downloader {

	void setManager(LoaderManager manager);
	InputStream downloadStream(UriRequest data,
			CusListener.failedListener listener) throws IOException;
	Response downloadStream(final UriRequest data) throws IOException;
	void loadStream(final UriRequest data, StreamCallback streamCallback);

	public static class Response {
		final InputStream stream;
		final long contentLength;

		/**
		 * Response stream and info.
		 * 
		 * @param stream
		 *            Image data stream.
		 * @param length
		 *            The content length of the response,
		 */
		public Response(InputStream stream, long length) {
			if (stream == null) {
				throw new IllegalArgumentException("Stream should not be null.");
			}
			this.stream = stream;
			this.contentLength = length;
		}

		/**
		 * Input stream containing image data.
		 */
		public InputStream getInputStream() {
			return stream;
		}

		/** Content length of the response. */
		public long getContentLength() {
			return contentLength;
		}
	}
}
