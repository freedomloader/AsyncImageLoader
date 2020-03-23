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
package com.freedom.asyncimageloader.callback;

import java.util.ArrayList;
import java.util.List;
import android.os.Handler;

import com.freedom.asyncimageloader.AsyncImage;
import com.freedom.asyncimageloader.listener.ImageLoaderStatusListener;

public class LoaderStatusCallback {

    private final List<ImageLoaderStatusListener> scb;
    
    private CallType type;
    private Handler handler;
	
    public LoaderStatusCallback() {
      handler = new Handler();
      scb = new ArrayList<ImageLoaderStatusListener>();
    }
    
    LoaderStatusCallback init(ImageLoaderStatusListener cb) {
      if(cb != null && !this.scb.contains(cb)) {
    	 registerLoaderListener(cb);
      }
	  return this;
	}
    
    public LoaderStatusCallback type(CallType type) {
	    this.type = type;
		return this;
	}
    
    public boolean onStart(AsyncImage loader) {
    	if(!cbEnabled()) return false;
    	return type(CallType.START).run(loader);
	}
    
    public boolean onPause(AsyncImage loader) {
    	if(!cbEnabled()) return false;
    	return type(CallType.PAUSE).run(loader);
	}
    
    public boolean onResume(AsyncImage loader) {
    	if(!cbEnabled()) return false;
    	return type(CallType.RESUME).run(loader);
	}
    
    public boolean onShutdown(AsyncImage loader) {
    	if(!cbEnabled()) return false;
    	return type(CallType.SHUTDOWN).run(loader);
	}
    
    public boolean onDestory(AsyncImage loader) {
    	if(!cbEnabled()) return false;
    	return type(CallType.DESTORY).run(loader);
	}

	private boolean run(final AsyncImage asyncImage) {
		Runnable rCallback = new Runnable() {
	     @Override public void run() {
	       switch (type) {
	        case START:
	          for (int i = 0, n = scb.size(); i < n; i++) {
	        	   scb.get(i).onCreate(asyncImage);
	    	  }
	       break;
	        case PAUSE:
	          for (int i = 0, n = scb.size(); i < n; i++) {
		           scb.get(i).onPause(asyncImage);
		      }
	       break;
	        case RESUME:
	          for (int i = 0, n = scb.size(); i < n; i++) {
			       scb.get(i).onResume(asyncImage);
			  }
	       break;
	        case SHUTDOWN:
	          for (int i = 0, n = scb.size(); i < n; i++) {
	        	   scb.get(i).onShutdown(asyncImage);
			  }
	       break;
	        case DESTORY:
		      for (int i = 0, n = scb.size(); i < n; i++) {
		           scb.get(i).onDestroy(asyncImage);
			  }
		   break;
	       default:
	    	   break;
	        }
	      }
	    };
	    handler.post(rCallback);
		return true;
	}
	
   public void registerLoaderListener(ImageLoaderStatusListener callback) {
	  if(callback != null)
	     scb.add(callback);
   }
   
   public void unRegisterLoaderListener(ImageLoaderStatusListener callback) {
	  scb.remove(callback);
   }
   
   private boolean cbEnabled() {
	  return scb != null && !scb.isEmpty() && scb.size() != 0;
   }
   
   enum CallType{
	     PAUSE,RESUME,SHUTDOWN,DESTORY,START
   }
 }