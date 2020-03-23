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
package com.freedom.asyncimageloader.disc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.freedom.asyncimageloader.exception.FailedReadingException;
import com.freedom.asyncimageloader.listener.CusListener;
import com.freedom.asyncimageloader.utils.Logg;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

public class DefaultDiscWriter implements DiscWriter {
	final int IO_BUFFER_SIZE = 32 * 1024;
	
	/**
	 * @param file file path
	 * @param bitmap bitmap to compress
	 * 
	 * @throws IOException if error occurred
	 */
	@Override
	public boolean writeFile(File file,Bitmap bitmap) throws IOException {
		CompressFormat compressFormat = CompressFormat.JPEG;
		return writeFile(file,bitmap,compressFormat,100);
	}
	
	/**
     * Stores the given bitmap. The file path already set.
     * This method uses the same naming convention for resized images with resize(width,height)
     * 
	 * @param file file path
	 * @param bitmap bitmap to compress
	 * @param compressFormat bitmap Compress Format
	 * @param quality bitmap quality
	 * 
	 * @throws IOException if error occurred
	 */
	@Override
	public boolean writeFile(File file,Bitmap bitmap,CompressFormat compressFormat,int quality) throws IOException {
		if (compressFormat == null) {
		    compressFormat = CompressFormat.JPEG;
		}
		if (quality == 0) {
			quality = 90;
		}
		if (bitmap == null) {
			return false;
		}
		try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(compressFormat, quality, out);
        } catch (Exception e) {
            Logg.warning("" + e.getMessage());
        }
		return true;
	}
	
	/**
	 * @param input file InputStream
	 * @param output file OutputStream
	 * @param listener listener for listen to file write
	 * 
	 * @throws IOException if error occurred during stream Copies
	 */
	public boolean writeOutput(InputStream input, OutputStream output,
			CusListener.BytesListener listener) throws IOException {
		int current = 0;
		final int lenghtOfFile = input.available();

		final byte[] bytes = new byte[IO_BUFFER_SIZE];
		int count;
		try {
			checkLoading(listener, current, lenghtOfFile);
			while ((count = input.read(bytes, 0, IO_BUFFER_SIZE)) != -1) {
				output.write(bytes, 0, count);
				current += count;
				checkLoading(listener, current, lenghtOfFile);
			}
		} catch (FailedReadingException e) {
			return false;
		}
		return true;
	}
	
	/**
	 * @param bytesListener  copying bytes to listener
	 * @param current  loaded bytes
	 * @param lenghtOfFile  max size of file
	 * 
	 * @throws FailedReadingException if should interrupt by listener
	 */
	private boolean checkLoading(CusListener.BytesListener bytesListener,
			int current, int lenghtOfFile) throws FailedReadingException {
		if (bytesListener != null) {
			//check if progress callback is not null if is null return false
			//check if task interrupted or view not active if true then throw FailedReadingException()
			if (!bytesListener.onBytesUpdate(current, lenghtOfFile)) {
				//check if current bytes match 75% do nothing
				if (100 * current / lenghtOfFile < 75) {
					//throw Exception if interrupt
					throw new FailedReadingException();
				}
			}
		}
		//successful copying
		return false;
	}
 }
