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

import android.os.Handler;

public class LoaderSet implements Runnable {
	LoaderManager manager;
	LoaderOptions options;
	int time;
	Type type;
	
	LoaderSet(LoaderManager manager,LoaderOptions options) {
		this.manager = manager;
		this.options = options;
	}
	
	public LoaderSet type(Type type) {
		this.type = type;
		return this;
	}
	
	public LoaderSet time(int time) {
		this.time = time;
		return this;
	}
	
	@Override public void run() {
		try {
		 switch(type) {
		   case PAUSE:
			  if(time != 0) {
				 timer(type,time);
			  }else {
				 manager.pause();
			  }
			  break;
		   case RESUME:
				manager.resume();
			  break;
		   case START:
				manager.start();
			  break;
		   case SHUTDOWN :
				manager.shutdown();
			  break;
		   case CLEAR_MEMORY:
				if(time != 0) {
				   timer(type,time);
				}else {
			       manager.pause();
			    }
			  break;
		  }
		} finally {
			this.type = null;
			this.time = 0;
		}
	}
	
	private void timer(final Type type,int time) {
	   new Handler().postDelayed(new Runnable() {
	     @Override public void run() {
	       if(type == Type.PAUSE) {
	  	      manager.resume();
	       }else if(type == Type.CLEAR_MEMORY) {
			  options.memoryCache.clear();
			}
	     }
	   },time);
	}
	
	enum Type {
		PAUSE, RESUME, START, SHUTDOWN, CLEAR_MEMORY
	}
}
