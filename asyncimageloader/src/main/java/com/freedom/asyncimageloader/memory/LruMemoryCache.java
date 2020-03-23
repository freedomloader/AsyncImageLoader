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
package com.freedom.asyncimageloader.memory;

import android.graphics.Bitmap;

import java.util.LinkedHashMap;
import java.util.Map;

import com.freedom.asyncimageloader.cache.DiscCache;
import com.freedom.asyncimageloader.cache.MemoryCache;
import java.util.*;

public class LruMemoryCache implements MemoryCache {

	private final LinkedHashMap<String, Bitmap> cacheMap;

	/** max size for memory cache */
	private final int maxSize;
	/** Disc cache */
	DiscCache discCache;
	/** Size of this cache in bytes */
	private int size;

	@Override
    public void init(String type,Object obj) {
  	  this.discCache = (DiscCache) obj;
    }
	
	/** @param maxSize for memory cache */
	public LruMemoryCache(int maxSize) {
		if (maxSize <= 0) {
			throw new IllegalArgumentException("maxSize <= 0");
		}
		this.maxSize = maxSize;
		this.cacheMap = new LinkedHashMap<String, Bitmap>(0, 0.75f, true);
	}

	/**
	 * Returns the Bitmap for {@code key} if it exists.
	 */
	@Override
	public final Bitmap get(String key) {
		if (key == null) {
			throw new NullPointerException("key == null");
		}

		synchronized (this) {
			return cacheMap.get(key);
		}
	}

	/** Caches {@code Bitmap} for {@code key}. */
	@Override
	public final void put(String key, Bitmap value) {
		if (key == null || value == null) {
			throw new NullPointerException("key == null || value == null");
		}

		synchronized (this) {
			size += sizeOf(key, value);
			Bitmap previous = cacheMap.put(key, value);
			if (previous != null) {
				size -= sizeOf(key, previous);
			}
		}

		trimToSize(maxSize);
	}
	
	/**
     * @param maxSize the maximum size of the cache before returning. May be -1 to evict even 0-sized elements.
     */
	private void trimToSize(int maxSize) {
		while (true) {
			String key;
			Bitmap value;
			synchronized (this) {
				if (size < 0 || (cacheMap.isEmpty() && size != 0)) {
					throw new IllegalStateException(getClass().getName() + ".sizeOf() is reporting inconsistent results!");
				}

				if (size <= maxSize || cacheMap.isEmpty()) {
					break;
				}

				Map.Entry<String, Bitmap> toEvict = cacheMap.entrySet().iterator().next();
				if (toEvict == null) {
					break;
				}
				key = toEvict.getKey();
				value = toEvict.getValue();
				cacheMap.remove(key);
				size -= sizeOf(key, value);
			}
		}
	}

	/**
     * Removes the entry for {@code key} if it exists.
     *
     * @return the previous value mapped by {@code key}.
     */
	@Override
	public final void remove(String key) {
		if (key == null) {
			throw new NullPointerException("key == null");
		}

		synchronized (this) {
			Bitmap previous = cacheMap.remove(key);
			if (previous != null) {
				size -= sizeOf(key, previous);
			}
		}
	}
	
	@Override
	public void clear() {
		trimToSize(-1); // -1 will evict 0-sized elements
	}

	/**
	 * Returns the size {@code Bitmap} in bytes.
	 * <p/>
	 * An entry's size must not change while it is in the cache.
	 */
	private int sizeOf(String key, Bitmap value) {
		return value.getRowBytes() * value.getHeight();
	}
	
	@Override
	public synchronized final String toString() {
	    return String.valueOf("LruCache : maxSize: "+maxSize);
	}

	@Override
	public int getCacheSize() {
		// TODO Auto-generated method stub
		return cacheMap.size();
	}

	@Override
	public List<String> retrieveListOfCacheKey() {
		List<String> listKeys = new ArrayList<String>();
		for (String key : cacheMap.keySet()) {
			listKeys.add(key);
		}
		return listKeys;
	}
	
	@Override
	public void onLowMemory() {
		// TODO Auto-generated method stub
	}
}
