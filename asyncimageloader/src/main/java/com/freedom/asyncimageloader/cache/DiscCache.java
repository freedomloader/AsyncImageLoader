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
package com.freedom.asyncimageloader.cache;

import java.io.File;

import com.freedom.asyncimageloader.utils.FileNameGenerator;

import android.content.Context;
 
public interface DiscCache {

	void init(File cacheDir,FileNameGenerator fileNameGenerator);
    File getFile(String filekey);
    File getFile(String filekey,String cacheExtension);
    void put(String key, File data);
    void clear();
    void cleanCache(Context con,long mills);
    File getStorage();
	long getDirSize();
    
}