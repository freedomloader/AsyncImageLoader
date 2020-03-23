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

public class FImgConstants {
	static String TASK_ALREADY_RUNNING = "Cannot submit task:" + " the task is already running.";
	static String TASK_ALREADY_FINISHED = "Cannot submit task:" + " the task has already been executed "
		       + "(a task can be executed only once)";

	static final int IO_BUFFER_SIZE = 32 * 1024;
	static final String LOAD_IMAGE_FROM_NETWORK = "Load image from network";
	static final String LOADING_IMAGE_FROM_NETWORK = "Image is loading from network";
	static final String LOADING_IMAGE_FROM_DISC = "Image is loading from disc";
	static final String LOAD_IMAGE_FROM_DISC_CACHE = "Load image from disc cache";
	static final String LOAD_IMAGE_FROM_MEMORY_CACHE = "Load image from memory cache";
	static final String IMAGE_CACHED_IN_MEMORY = "loading image cache in memory";
	static final String DECODE_IMAGE_FILE = "Decode image from disc cache";
	static final String START_LOADING_IMAGE_WAITING_TO_LOADED_AND_DISPLAY = "Image is already loading. Waiting for display... ";
	static final String PAUSE_LOADING_THREAD_TILL_WHEN_RESUME_CALL = "Pause loading thread till when resume call";
	static final String PAUSE_TASK_HAS_BEEN_RESUME = "Pause task is resume";
	static final String IMAGE_FAIL_TO_LOAD= "loading task image fail to load ";
	static final String IMAGE_OUT_OF_MEMORY = "Out of memory status reset memory cache";
	static final String NETWORK_DENIED = "Network denied you have set to use only local loading";
	static final String LOCAL_DENIED = "local denied you have set to use only network loading";
	
	static final String TASK_THREAD_INTERRUPTED_WITH_URI = "thread was interrupted";
	static final String TASK_IMAGEVIEW_REUSED = "ImageView is reused for another loading task.";
	static final String IMAGE_LOADING_FAILED = "Uri failed to load so we block Uri from loading for a well";
	static final String IMAGE_DECODING_FAILED = "Exception throw when decoding image";
	static final String IMAGE_VIEW_NOT_ACTIVE = "image view not active";
	static final String TASK_WAS_INTERRUPTED = "loading task was interrupted";
	static final String TASK_WAS_CANCELLED = "loading task is cancelled";
	static final String TASK_PAUSE_WAITING_TO_RESUME = "task pause waiting to resume";
	static final String DELAY_URI_BEFORE_LOADING = "uri is delay before loading";
	static final String BLOCK_URI_NOT_LOADING = "Current loading uri is block";
	static final String LOADER_SOME_ERROR_EXCEPTION = "ERROR EXCEPTION: ";
	
	static final String LOADER_COMPLETED = "completed";
	static final String LOADER_ERRORED = "errored";
	static final String LOADER_SHUTDOWN = "image loader stop";
	static final String LOADER_DESTORY = "image loader destory";
	static final String LOADER_PAUSE = "image loader pause loading";
	static final String LOADER_RESUME = "image loader resume loading";
	static final String LOADER_START = "image loader start again";
	static final String CONTEXT_REASON = "you dont set context or you are using ImageLoad.startLoader() with ImageLoad.with().";
	static final String CONFIG_METHOD_CALL = "you can only call ImageLoad.startLoader() only when you init in your application";
	static final String CONTEXT_NULL = "Context must not be null reason : "+CONTEXT_REASON+" cause : "+CONFIG_METHOD_CALL;
}
