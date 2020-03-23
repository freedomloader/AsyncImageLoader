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

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import com.freedom.asyncimageloader.utils.Logg;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
 
public class DiscMemoryCacheOptions {
	public int maxWidthForMemory = 0;
	public int maxHeightForMemory = 0;
	public int maxWidthForDisc = 0;
	public int maxHeightForDisc = 0;
	public CompressFormat bitmapCompressFormat;
	public long cacheMemoryForMills = 0;
	public long cacheDiscForMills = 0;
	public int maxMemoryCacheSize = 0;
	public int maxDiscCacheSize = 0;
	public int maxfileCountOnDisc = 0;
	public int discCompressQuality = 0;
	final Map<String, Long>  memoryCacheDuration = Collections.synchronizedMap(new WeakHashMap<String, Long>());

	  /**
       * Set max memory cache height and width size.
       *
       * @param maxWidthForMemory maximum target width size for memory cache
       * @param maxHeightForMemory maximum target height size for memory cache
       * @return self
       */ 
	   public DiscMemoryCacheOptions targetSizeForMemoryCache(int maxWidthForMemory,int maxHeightForMemory) {
	 		if (maxWidthForMemory <= 0) {
	 			Logg.warning("Width must be positive number.");
	 	    }
	 	    if (maxHeightForMemory <= 0) {
	 	        Logg.warning("Height must be positive number.");
	 	    }
	 		this.maxWidthForMemory = maxWidthForMemory;
	 		this.maxHeightForMemory = maxHeightForMemory;
	 		return this;
	 	}

	 	/**
         * Set the compression quality for Bitmaps when writing them out to the disk cache using writer.
         * This will only apply to bitmaps saved to the disk cache
         * @see Bitmap#compress(android.graphics.Bitmap.CompressFormat, int, java.io.OutputStream)
         *
         * @param maxWidthForDisc set maximum target width size for disc cache
         * @param maxHeightForDisc set maximum target height size for disc cache
         * @param discCompressQuality The format to pass to
         *  {@link Bitmap#compress(android.graphics.Bitmap.CompressFormat, int, java.io.OutputStream)} when saving
         *  to the disk cache
         * @param discCompressQuality Hint for compression in range 0-100 with 0 being lowest and 100 being highest quality.
         * @return self
         */
	 	public DiscMemoryCacheOptions targetOptionsForDiscCache(int maxWidthForDisc,int maxHeightForDisc,
	 			CompressFormat compressFormat,int discCompressQuality) {
	 		if (maxWidthForDisc <= 0) {
	 			Logg.warning("Width must be positive number.");
	 	    }
	 	    if (maxHeightForDisc <= 0) {
	 	    	Logg.warning("Height must be positive number.");
	 	    }
	 	    if (compressFormat == null) {
				Logg.warning("CompressFormat must not be null");
			}
	 	    if (discCompressQuality == 0) {
				Logg.warning("discImageQuality must not be 0");
			}
	 		this.discCompressQuality = discCompressQuality;
	 		this.bitmapCompressFormat = compressFormat;
	 		this.maxWidthForDisc = maxWidthForDisc;
	 		this.maxHeightForDisc = maxHeightForDisc;
	 		return this;
	 	}

	 	/**
         * Set expire duration time for memory cache.
         *
         * @param uri URLEncoded to cache to memory with duration
         * @param memoryCacheDuration cache duration time
         * @return self
         */
	 	public DiscMemoryCacheOptions memoryCacheDuration(Uri uri,long memoryCacheDuration) {
	 		this.memoryCacheDuration.put(uri.toString(), memoryCacheDuration);
	 		return this;
	 	}
	 	
	 	/**
         * Set expire duration time for memory cache.
         *
         * @param cacheMemoryForMills cache duration time
         * @return self
         */
	 	public DiscMemoryCacheOptions memoryCacheDuration(long cacheMemoryForMills) {
	 		if (cacheMemoryForMills == 0) {
	 	        Logg.warning("mills msut not be 0 .");
	 	    }
	 		this.cacheMemoryForMills = cacheMemoryForMills;
	 		return this;
	 	}
	 	
	 	/**
         * Set expire duration time for disc cache.
         *
         * @param cacheDiscForMills cache duration time
         * @return self
         */
	 	public DiscMemoryCacheOptions discCacheDuration(long cacheDiscForMills) {
	 		if (cacheDiscForMills == 0) {
	 	        Logg.warning("mills msut not be 0 .");
	 	    }
	 		this.cacheDiscForMills = cacheDiscForMills;
	 		return this;
	 	}
	 	
	 	/**
	     * Setting disc cache size the default disc size is 50mb 
	     *
	     * @param discCacheSize
         * @return self
	    */
	 	public DiscMemoryCacheOptions discCacheSize(int discCacheSize) {
			if (discCacheSize <= 0) 
				Logg.warning("discCacheSize cache size must be positive number");

			this.maxDiscCacheSize = discCacheSize;
			return this;
		}
	 	
	    /**
	     * Setting memory cache size the default memory size to 50% percent of the total memory
	     * available of the application.
	     *
	     * @param maxMemoryCacheSize
	    */
	 	public DiscMemoryCacheOptions memoryCacheSize(int maxMemoryCacheSize) {
			if (maxMemoryCacheSize <= 0) 
				Logg.warning("memory cache size must be positive number");
			
			if(maxMemoryCacheSize != 0) 
				Logg.warning("Memory cache size already set");
			
			this.maxMemoryCacheSize = maxMemoryCacheSize;
			return this;
		}
	 	
	 	/**
	     * Setting memory cache size the default memory size to 50% percent of the total memory
	     * available of the application.
	     *
	     * @param percentageOfMemoryForCache
	     */
		public DiscMemoryCacheOptions memoryCachePercentage(int percentageOfMemoryForCache) {
			if (percentageOfMemoryForCache <= 0 || percentageOfMemoryForCache >= 100) {
				Logg.warning("percentageOfMemoryForCache 1-90");
			}
			if(maxMemoryCacheSize != 0) {
				Logg.warning("Memory cache size already set");
			}
			long availableCacheMemory = Runtime.getRuntime().maxMemory();
			maxMemoryCacheSize = (int) (availableCacheMemory * (percentageOfMemoryForCache / 100f));
			return this;
		}
		
		/**
	     * Sets disc cache file count.
	     *
	     * @param maxfileCountOnDisc count of file to be cache in disc
	     */
		public DiscMemoryCacheOptions maxfileCountOnDisc(int maxfileCountOnDisc) {
			if(maxfileCountOnDisc == 0) {
				Logg.warning("max file Count cant be 0");
			}
			if(maxfileCountOnDisc != 0) {
				Logg.warning("max file Count already set");
			}
			this.maxfileCountOnDisc = maxfileCountOnDisc;
			return this;
		}
}