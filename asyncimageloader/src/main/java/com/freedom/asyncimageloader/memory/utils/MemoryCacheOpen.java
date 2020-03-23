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
package com.freedom.asyncimageloader.memory.utils;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;

import com.freedom.asyncimageloader.cache.DiscCache;
import com.freedom.asyncimageloader.cache.MemoryCache;

public class MemoryCacheOpen implements MemoryCache {
	private MemoryCache memoryCache;
	List<MemoryCacheListener> cacheCallbacks;

	public MemoryCacheOpen(DiscCache discCache,MemoryCache memoryCache,MemoryCacheListener memoryCallback) {
		this.memoryCache = memoryCache;
		init("",discCache);
		if (cacheCallbacks == null) {
			cacheCallbacks = new ArrayList<MemoryCacheListener>(3);
	    }
		if (memoryCallback != null) {
		    init("addCacheCallback",memoryCallback);
		}
	}
	
	@Override
	public void init(String type,Object obj) {
		if(type.equals("addCacheCallback")) {
		   MemoryCacheListener callback = (MemoryCacheListener) obj;
		   if(!cacheCallbacks.contains(callback.hashCode()) && callback != null) {
			   cacheCallbacks.add(callback);
		   }
		} else if(type.equals("removeCallback")) {
		   for (MemoryCacheListener callback : cacheCallbacks) {
				if(callback.equals((MemoryCacheListener) obj)) {
				   cacheCallbacks.remove(callback);
				}
		   }
		} else {
		   memoryCache.init(type,obj);
		}
	}

	@Override
	public void put(String uri, Bitmap bitmap) {
		memoryCache.put(uri, bitmap);
		if(callbacksEnabled()) {
		   for (MemoryCacheListener callback : cacheCallbacks) {
				 callback.onBitmapCache(uri, bitmap);
	       }
		}
	}

	@Override
	public Bitmap get(String uri) {
		return memoryCache.get(uri);
	}

	@Override
	public int getCacheSize() {
		if(callbacksEnabled()) {
		 for (MemoryCacheListener callback : cacheCallbacks) {
			  callback.onMemorySize(memoryCache.getCacheSize());
	     }
		}
		return memoryCache.getCacheSize();
	}

	@Override
	public void clear() {
		memoryCache.clear();
		if(callbacksEnabled()) {
		   for (MemoryCacheListener callback : cacheCallbacks) {
			    callback.onCacheClear(memoryCache);
		   }
		}
	}

	@Override
	public void remove(String key) {
		memoryCache.remove(key);
		if(callbacksEnabled()) {
		   for (MemoryCacheListener callback : cacheCallbacks) {
				callback.onRemove(key);
		   }
	   }
	}

	@Override
	public List<String> retrieveListOfCacheKey() {
		return memoryCache.retrieveListOfCacheKey();
	}
	
	@Override
	public void onLowMemory() {
		memoryCache.onLowMemory();
		if(callbacksEnabled()) {
		   for (MemoryCacheListener callback : cacheCallbacks) {
				callback.onLowMemory(memoryCache);
		   }
		}
	}
	
	@Override
	public String toString() {
		return memoryCache.toString();
	}
	
	private boolean callbacksEnabled() {
		return !cacheCallbacks.isEmpty() && callbackSize() != 0;
	}
	
	private int callbackSize() {
		return cacheCallbacks.size();
	}
}
