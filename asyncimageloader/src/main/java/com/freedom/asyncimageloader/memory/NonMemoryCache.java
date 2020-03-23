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
import com.freedom.asyncimageloader.cache.MemoryCache;

import android.graphics.Bitmap;
import java.util.*;

/**
 * This cache do not keep bitmap in memory.
 * Can only be invoked if should not cache images.
 */
public class NonMemoryCache implements MemoryCache {

	@Override
    public void init(String type,Object obj) {
    }

	@Override
	public void put(String key, Bitmap bitmap) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Bitmap get(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void remove(String key) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public int getCacheSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<String> retrieveListOfCacheKey() {
		List<String> listKeys = new ArrayList<String>();
		return listKeys;
	}
	
	@Override
	public void onLowMemory() {
		// TODO Auto-generated method stub
		
	}
}
