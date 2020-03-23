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

import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

public class FreedomScrollListener implements OnScrollListener {

	private int pauseTime = 0;
	private boolean pauseOnScroll;
	private boolean pauseOnIdle;
	private boolean pauseOnFling;
	private AsyncImage loader;
	private OnScrollListener customScrollListener;

	public FreedomScrollListener(AsyncImage loader) {
		this(loader,null,false,true,true);
	}
	
	public FreedomScrollListener(AsyncImage loader,boolean pauseOnScroll,boolean pauseOnFling,boolean pauseOnIdle) {
		this(loader,null,pauseOnScroll,pauseOnFling,pauseOnIdle);
	}
	
	public FreedomScrollListener(AsyncImage loader,OnScrollListener customScrollListener,boolean pauseOnScroll,
			boolean pauseOnFling,boolean pauseOnIdle) {
		this.loader = loader;
		setScrollListener(null);
		setPauseOnScroll(pauseOnScroll);
		setPauseOnFling(pauseOnFling);
		setPauseOnIdle(pauseOnIdle);
	}
	
	public FreedomScrollListener setScrollListener(OnScrollListener customScrollListener) {
		this.customScrollListener = customScrollListener;
		return this;
	}
	
	public FreedomScrollListener setPauseOnScroll(boolean pauseOnScroll) {
		this.pauseOnScroll = pauseOnScroll;
		return this;
	}
	
	public FreedomScrollListener setPauseOnFling(boolean pauseOnFling) {
		this.pauseOnFling = pauseOnFling;
		return this;
	}
	
	public FreedomScrollListener setPauseOnIdle(boolean pauseOnIdle) {
		this.pauseOnIdle = pauseOnIdle;
		return this;
	}
	
	public FreedomScrollListener setPauseTime(int pauseTime) {
		this.pauseTime = pauseTime;
		return this;
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		setOnScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
	}
	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		switch (scrollState) {
		    case OnScrollListener.SCROLL_STATE_FLING:
			    if (pauseOnFling) {
				    pause();
		     	}
			    break;
			case OnScrollListener.SCROLL_STATE_IDLE:
				if (pauseOnIdle) {
					loader.resume();
				}
				break;
			case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
				if (pauseOnScroll) {
					 pause();
				}
				break;
				default:
					if(loader.isPause())
					   loader.resume();
		}
		setOnScrollStateChanged(view, scrollState);
	}
	
	private void pause() {
		if(pauseTime != 0) {
			loader.pause(pauseTime);
		}else {
			loader.pause();
		}
	}
	
	private void setOnScrollStateChanged(AbsListView view, int scrollState) {
		if (customScrollListener())
			customScrollListener.onScrollStateChanged(view, scrollState);
	}
	
	private void setOnScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if (customScrollListener())
			customScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
	}
	
	private boolean customScrollListener() {
		return customScrollListener != null;
	}
}