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

import com.freedom.asyncimageloader.cache.DiscCache;
import com.freedom.asyncimageloader.cache.MemoryCache;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.*;
import java.util.*;

public class LimitedMemoryCache implements MemoryCache {

	// Default memory cache size
    public static final int DEFAULT_MEMORY_CACHE_SIZE = 1024 * 1024 * 20; // 20MB

    public static final int FIVE_MB_MEMORY_CACHE_SIZE = 1024 * 1024 * 5; // 5MB
    public static final int TEN_MB_MEMORY_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
    
    public static final float DEFAULT_MEMORY_CACHE_PERCENTAGE = 0.25f;
    private static final int DEFAULT_CAPACITY_FOR_DEVICES = 12;
    private LruCache<String, Bitmap> cache;
    private int maxSize;
    int size;
    DiscCache discCache = null;
    
    public LimitedMemoryCache(Context context, int maxSize) {
        int  memSize = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
        this.maxSize = getCacheSize(memSize, maxSize);
        reload();
    }
    
    public LimitedMemoryCache(int maxSize) {
        this.maxSize = maxSize;
        reload();
    }
    
    public LimitedMemoryCache() {
       this.maxSize = DEFAULT_MEMORY_CACHE_SIZE;
       reload();
    }
	
    @Override
    public void init(String type,Object obj) {
  	  this.discCache = (DiscCache) obj;
    }

    private void reload() {
       if (cache != null) {
            cache.evictAll();
        }
        cache = new LruCache<String, Bitmap>(maxSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getRowBytes() * bitmap.getHeight();
            }
        };
    }

   @Override
	public Bitmap get(String key) {
    if (key == null) {
  	      throw new NullPointerException("key uri == null");
    }
    Bitmap mapValue;
    synchronized (this) {
      mapValue = cache.get(key);
      if (mapValue != null) {
        return mapValue;
      }
    }
    return null;
	}
    
    @Override
    public void put(String key, Bitmap bmp) {
         cache.put(key, bmp);
    }

    @Override
    public void remove(String key) {
         cache.remove(key);
    }

    @Override
	public void clear() {
    	reload();
    }

    public int getCacheSize(int memClass, int sizeLimit) {
        if (memClass == 0) {
        	memClass = DEFAULT_CAPACITY_FOR_DEVICES;
        }
        if (sizeLimit < 0) {
        	sizeLimit = 0;
        }
        if (sizeLimit > 81) {
        	sizeLimit = 80;
        }
        int limit = (int) ((memClass * sizeLimit * 1024L * 1024L) / 100L);
        if (limit <= 0) {
        	limit = 1024 * 1024 * 5;
        }
        return limit;
    }
    
    @Override
	public int getCacheSize() {
		// TODO Auto-generated method stub
		return cache.size();
	}

	@Override
	public List<String> retrieveListOfCacheKey() {
		List<String> listKeys = new ArrayList<String>();
		for (int i = 0, n = getCacheSize(); i < n; i++) {
			    Bitmap bitmap = cache.get("");
				String key = bitmap != null ? "" : "";
			    listKeys.add(key);
		}
		return listKeys;
	}
	
	@Override
	public void onLowMemory() {
		// TODO Auto-generated method stub
	}
}
