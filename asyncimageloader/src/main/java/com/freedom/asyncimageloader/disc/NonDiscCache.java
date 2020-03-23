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

import com.freedom.asyncimageloader.cache.DiscCache;
import com.freedom.asyncimageloader.utils.FileNameGenerator;

import android.content.Context;
 
public class NonDiscCache implements DiscCache {

	@Override
	public void init(File cacheDir, FileNameGenerator fileNameGenerator) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public File getFile(String filekey) {
		// TODO Auto-generated method stub
		return new File(filekey);
	}
	
	@Override
	public File getFile(String filekey,String extension) {
		// TODO Auto-generated method stub
		return new File(filekey);
	}

	@Override
	public void put(String key, File data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void cleanCache(Context con, long mills) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public File getStorage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getDirSize() {
		// TODO Auto-generated method stub
		return 0;
	}
}
