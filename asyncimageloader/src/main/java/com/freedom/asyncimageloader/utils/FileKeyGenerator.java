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
package com.freedom.asyncimageloader.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.freedom.asyncimageloader.ImageOptions;
import com.freedom.asyncimageloader.LoaderSettings;
import com.freedom.asyncimageloader.assist.Resizer;
import com.freedom.asyncimageloader.*;
public class FileKeyGenerator {

	public static String FIND_CACHE_KEY = "multi-uri";
	public static String FIND_CACHE_SIZE_KEY = "cache-resize:";
	public static class MD5 implements FileNameGenerator {
		/** Generates MD5 key*/
	    public String generateKey(String key) {
	        String cacheKey;
	        try {
	            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
	            mDigest.update(key.getBytes());
	            cacheKey = bytesToHexString(mDigest.digest());
	        } catch (NoSuchAlgorithmException e) {
	            cacheKey = String.valueOf(key.hashCode());
	        }
	        return cacheKey;
	    }

	    /** Read from bytes To Hex String */
	    private String bytesToHexString(byte[] bytes) {
	        StringBuilder sb = new StringBuilder();
	        for (int i = 0; i < bytes.length; i++) {
	            String hex = Integer.toHexString(0xFF & bytes[i]);
	            if (hex.length() == 1) {
	                sb.append('0');
	            }
	            sb.append(hex);
	        }
	        return sb.toString();
	    }
	}

	public static class HASH implements FileNameGenerator {
		/** Generates Disc Cache key*/
		public String generateKey(String key) {
			int hash = key.hashCode();
			return String.valueOf(hash);
		}
	}

    /** Generates memoryCache key with width and height if required*/
	public static String generateMemoryCacheKey(String key, Resizer size, LoaderSettings setting,boolean shouldAllowMultipleSizes) {
		StringBuffer builder = new StringBuffer();
		ImageOptions options = setting.getImageOptions();

		if(shouldAllowMultipleSizes) {
			//allow multiple size
			builder.append(key);
			builder.append('\n');
			builder.append(FIND_CACHE_KEY);

			if (options.rotationDegrees != 0) {
				builder.append("rotation:").append(options.rotationDegrees);
				builder.append('\n');
			}
			if (options.rounded != 0) {
				builder.append("rounded:").append(options.rounded);
				builder.append('\n');
			}
			if (options.grayScale) {
				builder.append("grayScale:").append(options.grayScale);
				builder.append('\n');
			}
			if (options.blackAndWhite) {
				builder.append("blackWhite:").append(options.blackAndWhite);
				builder.append('\n');
			}
			if (setting.transform != null) {
				builder.append("transform:").append(setting.transform.transformKey());
				builder.append('\n');
			}
			if (options.centerCrop) {
				builder.append("centerCrop\n");
			} else if (options.centerInside) {
				builder.append("centerInside\n");
			}
			if (size != null) {
				builder.append(FIND_CACHE_SIZE_KEY).append(size.width).append('x').append(size.height);
				builder.append('\n');
			}
			if (setting.shouldMargeBitmap()) {
				builder.append(AsyncImage.FREEDOM_MARGE_BITMAPS_URI).append(setting.getMargeBitmap().getKey());
				builder.append('\n');
			}
			//deny multiple size
		} else {
			builder.append(key);
			builder.append(FIND_CACHE_KEY);
		}
		return builder.toString();
	}

}
