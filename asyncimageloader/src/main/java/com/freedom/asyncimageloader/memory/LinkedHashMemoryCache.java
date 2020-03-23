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

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.freedom.asyncimageloader.cache.DiscCache;
import com.freedom.asyncimageloader.cache.MemoryCache;
import com.freedom.asyncimageloader.utils.Logg;

import android.graphics.Bitmap;
import java.util.*;

public class LinkedHashMemoryCache implements MemoryCache {
	DiscCache discCache = null;

	private Map<String, Bitmap> cache = Collections.synchronizedMap(new LinkedHashMap<String, Bitmap>(10, 1, true)); 
	private long size = 0; // current allocated size
	private long limit = 1000000; // max memory in bytes

	public LinkedHashMemoryCache() {
		// use 25% of available heap size
		setLimit(Runtime.getRuntime().maxMemory() / 4);
	}

	public LinkedHashMemoryCache(int maxSize) {
		setLimit(maxSize);
	}
	 
	@Override
    public void init(String type,Object obj) {
  	  this.discCache = (DiscCache) obj;
    }
	
	public void setLimit(long maxlimit) {
		limit = maxlimit;
		Logg.info("MemoryCache will use up to " + limit / 1024. / 1024. + "MB");
	}

	public Bitmap get(String id) {
		try {
			if (!cache.containsKey(id))
				return null;
			// NullPointerException sometimes happen here http:// code.google.com/p/osmdroid/issues/detail?id=78
			return cache.get(id);
		} catch (NullPointerException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public void put(String id, Bitmap bitmap) {
		try {
			if (cache.containsKey(id))
				size -= getSizeInBytes(cache.get(id));
			cache.put(id, bitmap);
			size += getSizeInBytes(bitmap);
			checkSize();
		} catch (Throwable th) {
			th.printStackTrace();
		}
	}

	private void checkSize() {
		Logg.info( "cache size=" + size + " length=" + cache.size());
		if (size > limit) {
			Iterator<Entry<String, Bitmap>> iter = cache.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<String, Bitmap> entry = iter.next();
				size -= getSizeInBytes(entry.getValue());
				iter.remove();
				if (size <= limit)
					break;
			}
			Logg.info("Clean cache. New size " + cache.size());
		}
	}

	@Override
	public void remove(String key) {
		cache.remove(key);
		size -= 1;
	}
	
	public void clear() {
		try {
			// NullPointerException sometimes happen here http:// code.google.com/p/osmdroid/issues/detail?id=78
			cache.clear();
			size = 0;
		} catch (NullPointerException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public int getCacheSize() {
		// TODO Auto-generated method stub
		return cache.size();
	}
	
	private long getSizeInBytes(Bitmap bitmap) {
		if (bitmap == null)
			return 0;
		return bitmap.getRowBytes() * bitmap.getHeight();
	}

	@Override
	public List<String> retrieveListOfCacheKey() {
		List<String> listKeys = new ArrayList<String>();
		for (String key : cache.keySet()) {
			   listKeys.add(key);
		}
		return listKeys;
	}
	
	@Override
	public void onLowMemory() {
		// TODO Auto-generated method stub
	}

}
