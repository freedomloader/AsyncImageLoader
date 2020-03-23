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
package com.freedom.asyncimageloader;
import android.graphics.*;
import com.freedom.asyncimageloader.exception.*;
import java.io.*;
import com.freedom.asyncimageloader.uri.*;
import com.freedom.asyncimageloader.assist.*;
import com.freedom.asyncimageloader.interfaces.*;
import java.util.concurrent.*;
import com.freedom.asyncimageloader.listener.*;
import com.freedom.asyncimageloader.callback.*;


public interface PhotoTask {
	
	/** quit incoming or uncompleted loading thread.*/
	void quitThread();
	void cancel();
	boolean isTaskCancelled();
	void execute(Executor executor);
	
	 boolean delete(File file);

	 LoaderCallback getCallback();
	ProgressCallback getProgressCallback();
	/**
	 * Check if should cache image to memory cache
	 * 
	 * Returns <tt>true</tt> - if should use cache for image
	 */
    boolean shouldUseMemoryCache();

	/**
	 * Check if should cache image to disc cache
	 * 
	 * Returns <tt>true</tt> - if should cache image to disc
	 */
    boolean shouldCacheOnDisc();

  	/**
	 * Check if smooth loading is enabled
	 * 
	 * Returns <tt>true</tt> - if smooth loading is enabled
	 */
  	boolean smoothLoadingEnabled();
	
  	/**
	 * Check if Should Fix Some Error
	 * 
	 * Returns <tt>true</tt> - if fix error is enabled
	 */
  	boolean shouldFixSomeError();
  	File getDiscCacheFile();

	/**
	 * Returns <tt>true</tt> - if images should be loaded from Internet
	 */
	boolean networkUri(String uri);

	/**
	 * Returns <tt>true</tt> - if status is pending
	 *
	 * @throws IllegalStateException If {#getStatus()} returns either
	 */
	boolean checkStatus();

	/**
	 * Returns the current status of this task.
	 *
	 * @return The current task status.
	 */
	TaskStatus getTaskStatus();

	/**
	 * Returns bitmap.
	 *
	 * @return bitmap.
	 */
	Bitmap getBitmap();

	/**
	 * Returns the current loading URLEncoded.
	 *
	 * @return The current loading URLEncoded.
	 */
	String currentLoadingUri();
	String getCacheKey();

	/**
	 * Returns the current request data.
	 *
	 * @return The current loading data.
	 */
	RequestData getData();

	/**
	 * Returns where the image Loaded from.
	 *
	 * @return loadedFrom.
	 */
	PhotoLoadFrom getLoadedFrom();
}
