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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import java.util.Map.Entry;
import android.os.Handler;

import com.freedom.asyncimageloader.interfaces.DataEmiter;
import com.freedom.asyncimageloader.interfaces.Failed;
import com.freedom.asyncimageloader.listener.LoadingListener;

public class MultiLoadedCallback {

    private Map<Integer, LoadingListener> scb;
	
    //private CallType type;
    private Handler handler;
	
	public MultiLoadedCallback() {
	  handler = new Handler();
	  scb = new LinkedHashMap<Integer, LoadingListener>();
    }
    
	MultiLoadedCallback init(LoadingListener cb) {
      if(cb != null && !this.scb.containsKey(cb.hashCode())) {
    	 registerLoaderListener(cb);
      }
	  return this;
	}
    
    public MultiLoadedCallback type(CallType type) {
	    //this.type = type;
		return this;
	}
    
    public boolean loaded(final DataEmiter dataEmiter) {
    	if(!cbEnabled()) return false;
    	return run(CallType.LOADED,dataEmiter);
	}
    
    public boolean failed(final Failed failed) {
    	if(!cbEnabled()) return false;
    	return run(CallType.FAILED,failed);
	}

	private boolean run(final CallType type,final Object object) {
		Runnable rCallback = new Runnable() {
	     @Override public void run() {
	       switch (type) {
	       case LOADED:
		       for (Map.Entry<Integer, LoadingListener> entry : entrySet()) {
		    	   entry.getValue().onLoaded((DataEmiter) object);
		       }
		   break;
		    case FAILED:
		       for (Map.Entry<Integer, LoadingListener> entry : entrySet()) {
		    	   entry.getValue().onFailed((Failed) object);
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
	
   public void registerLoaderListener(LoadingListener callback) {
		if(callback != null)
		  scb.put(callback.hashCode(),callback);
   }
	   
   public void unRegisterLoaderListener(LoadingListener callback) {
	  scb.remove(callback.hashCode());
   }
   
   public boolean cbEnabled() {
	  return scb != null && !scb.isEmpty() && scb.size() != 0;
   }
   
   private Set<Entry<Integer, LoadingListener>> entrySet() {
       return this.scb.entrySet();
   }
   
   enum CallType{
	     LOADED,FAILED
 } 
 }
