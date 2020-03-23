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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ExecutorOptions {
    boolean keepAliveSet = false;

	public static final int DEFAULT_THREAD_POOL_SIZE = 3;
	public static final int DEFAULT_THREAD_PRIORITY = Thread.NORM_PRIORITY - 1;
	
	public int threadPoolSize = 0;
	public int threadPriority = 0;
	public long keepAlive = 0;
	public TimeUnit timeUnit = null;
	public BlockingQueue<Runnable> taskQueue = null;

	/**
	 * keep alive time, which is a time after which an idle thread is eligible
	 * for being torn down.
	 */
	public ExecutorOptions keepAlive(long keepalive) {
		this.keepAliveSet = true;
		this.keepAlive = keepalive;
		return this;
	}

	/**
	 * Sets Executor timeUnit.
	 */
	public ExecutorOptions timeUnit(TimeUnit timeunit) {
		this.timeUnit = timeunit;
		return this;
	}

	/**
	 * task queue to hold tasks awaiting execution.
	 */
	public ExecutorOptions taskQueue(BlockingQueue<Runnable> taskQueue) {
		this.taskQueue = taskQueue;
		return this;
	}

	/**
	 * The thread pool size (the size the thread pool will try to stick with).
	 */
	public ExecutorOptions threadPoolSize(int threadPoolSize) {
		this.threadPoolSize = threadPoolSize;
		return this;
	}

	/**
	 * Sets the image priority for threads.
	 */
	public ExecutorOptions threadPriority(int threadPriority) {
		if (threadPriority < Thread.MIN_PRIORITY) {
			this.threadPriority = Thread.MIN_PRIORITY;
		} else {
			this.threadPriority = threadPriority;
		}
		return this;
	}
	
	/** Builds Executor results */
	public ExecutorOptions build() {
		if (threadPriority == 0) {
			this.threadPriority = DEFAULT_THREAD_PRIORITY;
		}

		if (threadPoolSize == 0) {
			this.threadPoolSize = DEFAULT_THREAD_POOL_SIZE;
		}

		if (taskQueue == null) {
			this.taskQueue = new LinkedBlockingQueue<Runnable>();
		}
		
		if(timeUnit == null) {
			this.timeUnit = TimeUnit.MILLISECONDS;
		}
		
		if(keepAlive == 0 && keepAliveSet == false) {
			this.keepAlive  = 0L;
		}
		return this;
	}
}