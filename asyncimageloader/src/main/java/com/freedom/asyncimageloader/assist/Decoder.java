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

import com.freedom.asyncimageloader.DiscMemoryCacheOptions;

import android.graphics.Bitmap;

public interface Decoder {
	/**
	 * Decodes image from file to bitmap.
	 *
	 * @return decodedBitmap
	 * @throws IOException
	 */
	 Bitmap decodeFile(BitmapDecoder.RequestForDecode decodeRequest) throws IOException;
	 Bitmap decodeFile(BitmapDecoder.RequestForDecode decodeRequest,InputStream downloaded) throws IOException;
	 Bitmap rotate(Bitmap bitmap,int rotate) throws IOException;
	 boolean saveImage(BitmapDecoder.RequestForDecode decodingInfo,DiscMemoryCacheOptions options) throws IOException ;
}
