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
import java.util.HashMap;
import java.util.Map;

import android.graphics.Bitmap;

import com.freedom.asyncimageloader.cache.DiscCache;
import com.freedom.asyncimageloader.cache.MemoryCache;
import com.freedom.asyncimageloader.utils.TimeUtils;
import com.freedom.asyncimageloader.utils.Utility;
import java.util.*;

public class TimeMemoryCache implements MemoryCache {
	private final MemoryCache memoryCache;

	private final long time;
	DiscCache discCache = null;
	private final Map<String, Long> cacheTime = Collections.synchronizedMap(new HashMap<String, Long>());

  @Override
  public void init(String type,Object obj) {
	  this.discCache = (DiscCache) obj;
	  memoryCache.init("",(DiscCache) obj);
  }
	
   public TimeMemoryCache() {
	    this(Utility.createMemoryCache(0),TimeUtils.MINUTE.TWO_MINUTE);
   }
 
   public TimeMemoryCache(long time) {
	    this(Utility.createMemoryCache(0),time);
   }
   
   public TimeMemoryCache(MemoryCache cache, long time) {
		this.memoryCache = cache; // set memory cache
		this.time = time; // set cache time
   }
	
	@Override
    public void put(String key, Bitmap bmp) {
		memoryCache.put(key, bmp);// put bitmap to memory cache
		cacheTime.put(key, System.currentTimeMillis());// put currentTimeMillis to cache time
    }

	@Override
	public Bitmap get(String key) {
		Long mTime = cacheTime.get(key);
		if (mTime != null && System.currentTimeMillis() - mTime > time) {
			remove(key);
		}
		return memoryCache.get(key);
	}

	@Override
	public void remove(String key) {
		memoryCache.remove(key); // remove from memory Cache
		cacheTime.remove(key); // remove from cache time
	}

	@Override
	public void clear() {
		memoryCache.clear(); // clear cache
		cacheTime.clear(); // clear cache time
	}

	@Override
	public int getCacheSize() {
		return cacheTime.size(); // get cache time size
	}

	@Override
	public List<String> retrieveListOfCacheKey() {
		return memoryCache.retrieveListOfCacheKey();
	}
	
	public String getStats() {
		return memoryCache.toString();
	}

	@Override
	public void onLowMemory() {
		// TODO Auto-generated method stub
	}
}
