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
import com.freedom.asyncimageloader.utils.Logg;
import com.freedom.asyncimageloader.utils.TimeUtils;
import com.freedom.asyncimageloader.utils.Utility;

import android.content.Context;
 
public class LruDiscCache implements DiscCache {

    // Default disk cache size
    static final int DEFAULT_DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
    
    private File cacheDir;
    Context context;
    static boolean isRemoved = false;
    static final String ANDROID_STORAGE_ROOT = "/Android/data/";
    static final String DEFAULT_IMAGE_FOLDER = "/cache/images";
    FileNameGenerator fileNameGenerator;
    
    public LruDiscCache(Context context){
    	this.context = context;
        //Create cache directory to save cached images
        if(this.cacheDir == null) {
           this.cacheDir = Utility.createDefaultDiscCacheDirectory(context);
    	}
    }
    
    @Override
	public void init(File cacheDir, FileNameGenerator fileNameGenerator) {
         this.cacheDir = cacheDir;
	  	 if(this.fileNameGenerator == null) {
	  		this.fileNameGenerator = fileNameGenerator;
	  	 }
	}
    
    public File getStorage() {
       return cacheDir;
    }

    public File getFile(String filekey) {
        File f = new File(cacheDir, fileNameGenerator.generateKey(filekey));
        return f;
    }
    
    @Override
	public File getFile(String filekey,String extension) {
    	File f = new File(cacheDir, fileNameGenerator.generateKey(filekey) +extension);
        return f;
	}
    
    public void clear(){
        File[] files=cacheDir.listFiles();
        if(files==null)
            return;
        for(File f:files)
            f.delete();
    }
    
    /**
     * Clear image from disc internal memory.
     * @param mills mills The max days or seconds or minute or more
     * older than this mills
     *              should be remove.
     */
    public void cleanCache(Context con,long mills) {
        if (isRemoved) {
            return;
        }
        if (!getStorage().isDirectory()) {
			return;
		}
        isRemoved = true;
        try {
    		File[] files = getStorage().listFiles();
            if (files == null) {
                return;
            }
    		for (File file : files) {
    			if (!file.isFile()) {
                    return;
                }

                if (System.currentTimeMillis() > file.lastModified() + mills) {
                	long time = file.lastModified();
             	    final long diff = TimeUtils.getCalendarTime().getTime() - time;
   	       		    Logg.info("Cache clear : time : "+TimeUtils.getMillsAgo(diff)+" mills : ");
   	       		    
                	file.delete();
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void setNomediaFile(File dir) {
        try {
            new File(dir, ".nomedia").createNewFile();
        } catch (Exception e) {
        }
    }

	@Override
	public void put(String key, File file) {
		
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
