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

import android.graphics.Bitmap;

import com.freedom.asyncimageloader.cache.MemoryCache;
import com.freedom.asyncimageloader.utils.Logg;

public class MemoryCacheCallback implements MemoryCacheListener {
	@Override
	public void onMemorySize(int maxSize) {
		Logg.info("cache.getCacheSize() is call ? size of memory cache");
	}

	@Override
	public void onLowMemory(MemoryCache cache) {
		Logg.info("cache.lowMemory() is call ? cache out of memory");
	}

	@Override
	public void onBitmapCache(String uri, Bitmap bitmap) {
		Logg.info("cache.put(uri,bitmap) is call ? new bitmap cache");
	}

	@Override
	public void onCacheClear(MemoryCache cache) {
		Logg.info("cache.clear() is call ? memory cache clear");
	}

	@Override
	public void onRemove(String key) {
		Logg.info("cache.remove() is call ? cache key remove");
	}
}