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
package com.freedom.asyncimageloader.listener;

import com.freedom.asyncimageloader.AsyncImage;
import com.freedom.asyncimageloader.utils.Logg;

public class ImageLoaderCallback implements ImageLoaderStatusListener {
	private AsyncImage loader;
	
	public ImageLoaderCallback(AsyncImage loader) {
		this.loader = loader;
	}
	
	@Override
	public void onCreate(AsyncImage loader) {
		Logg.info("ImageUnited.start() is call ? image loader created");
	}
	
	public void onPause(AsyncImage loader) {
		Logg.info("ImageUnited.pause() is call ? image loader pause");
	}

	public void onResume(AsyncImage loader) {
		Logg.info("ImageUnited.resume() is call ? image loader resume");
	}

	public void onShutdown(AsyncImage loader) {
		Logg.info("ImageUnited.shutdown() is call ? image loader shutdown");
	}

	public void onDestroy(AsyncImage loader) {
		Logg.info("ImageUnited.destory() is call ? image loader destory");
	}
	
	public void pause() {
		if(isLoaderEnabled()) loader.pause();
	}
	
	public void pause(int time) {
		if(isLoaderEnabled()) loader.pause(time);
	}
	
	public boolean resume() {
		return isLoaderEnabled() ? loader.resume() : false;
	}
	
	public void start() {
		if(isLoaderEnabled()) loader.start();
	}
	
	public void shutdown() {
		if(isLoaderEnabled()) loader.shutdown();
	}
	
	public void destroy() {
		if(isLoaderEnabled()) loader.destroy();
	}
	
	private boolean isLoaderEnabled() {
		return loader != null;
	}
}