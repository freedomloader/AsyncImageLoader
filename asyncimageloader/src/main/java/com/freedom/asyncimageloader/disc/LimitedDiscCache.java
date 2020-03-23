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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.freedom.asyncimageloader.cache.DiscCache;
import com.freedom.asyncimageloader.utils.FileNameGenerator;
import com.freedom.asyncimageloader.utils.Logg;

import android.content.Context;
import android.os.Handler;
 
public class LimitedDiscCache implements DiscCache {

    // Default disk cache size
    private static final int MAX_REMOVALS = 4;

    private int cacheSize = 0;
    private int cacheByteSize = 0;
    private final int maxCacheItemSize = 64; // 64 item default
    private long maxCacheByteSize = 1024 * 1024 * 5; // 5MB default
    File cacheDir;
    Context context;
    DiscCache discCache;
    Handler handler = new Handler();
    FileNameGenerator fileNameGenerator;

    private final Map<String, String> mFileHashMap =
            Collections.synchronizedMap(new LinkedHashMap<String, String>(
            		32,0.75f, true));
    
    public LimitedDiscCache(Context context,int discCacheSize){
    	this.maxCacheByteSize = discCacheSize;
    	this.context = context;
    	this.discCache = new LruDiscCache(context);
    }
    
    public LimitedDiscCache(Context context,DiscCache discCache,int discCacheSize){
    	this.maxCacheByteSize = discCacheSize;
    	this.context = context;
    	this.discCache = discCache;
    }
    
    @Override
	public void init(File cacheDir, FileNameGenerator fileNameGenerator) {
  	      discCache.init(cacheDir,fileNameGenerator);
	}
    
    protected boolean isMounted() {
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }
 
    public File getStorage() {
       return discCache.getStorage();
    }

    public File getFile(String filekey) {
       return discCache.getFile(filekey);
    }
    
    @Override
	public File getFile(String filekey,String extension) {
        return discCache.getFile(filekey,extension);
	}
    
    public void clear(){
		discCache.clear();
    }
    
    public void put(final String key, File data) {
        synchronized (mFileHashMap) {
            if (mFileHashMap.get(key) == null) {
                try {
                	File file = discCache.getFile(key);
                    if (file.exists()) {
                        put(key, file.getAbsolutePath());
                        flushDiscCache();
                    }
                } catch (final Exception e) {
                    Logg.error(e,"Error with: " + e.getMessage());
                }
            }
        }
    }

    private void put(String key, String file) {
        mFileHashMap.put(key, file);
        cacheSize = mFileHashMap.size();
        cacheByteSize += new File(file).length();
    }
    
	@Override
	public void cleanCache(Context con, long mills) {
		discCache.cleanCache(con, mills);
	}
	
	 private void flushDiscCache() {
	        Entry<String, String> eFileEntry;
	        File eFile;
	        long eFileSize;
	        int count = 0;

	        while (count < MAX_REMOVALS &&
	                (cacheSize > maxCacheItemSize || cacheByteSize > maxCacheByteSize)) {
	            eFileEntry = mFileHashMap.entrySet().iterator().next();
	            eFile = new File(eFileEntry.getValue());
	            eFileSize = eFile.length();
	            mFileHashMap.remove(eFileEntry.getKey());
	            eFile.delete();
	            cacheSize = mFileHashMap.size();
	            cacheByteSize -= eFileSize;
	            count++;
	        }
	    }
	 
	 @Override
		public long getDirSize() {
			long size = 0L;
			if (getStorage() == null) {
				return size;
			}
			if (getStorage().isDirectory()) {
				for (File file : getStorage().listFiles()) {
					size += file.length();
				}
			} else {
				size = getStorage().length();
			}
			return size;
		}
}
