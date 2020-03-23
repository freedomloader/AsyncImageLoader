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

import android.widget.ImageView;

import com.freedom.asyncimageloader.LoaderManager;
import com.freedom.asyncimageloader.LoaderSettings;
import com.freedom.asyncimageloader.LoaderManager.LoaderWorker;
import com.freedom.asyncimageloader.interfaces.DataEmiter;
import com.freedom.asyncimageloader.interfaces.Failed;
import com.freedom.asyncimageloader.listener.*;

import android.os.*;
import com.freedom.asyncimageloader.*;

public class WorkerCallback {
	LoaderManager manager;
	public MultiLoadedCallback multiCallback;
	String key;
	final boolean singleCallback;
	
    public WorkerCallback(LoaderManager manager,boolean singleCallback) {
    	this.singleCallback = singleCallback;
    	this.manager = manager;
    	multiCallback = new MultiLoadedCallback();
    }
    
    public void key(String key) {
    	this.key = key;
    }
    
    public WorkerCallback init(PhotoTask task,LoaderManager manager) {
    	this.manager = manager;
    	return this;
    }
    
    public boolean loaded(LoaderSettings setting,LoaderCallback cb,DataEmiter dataEmiter,ImageView view) {
		if (multiCallback.cbEnabled()) {
			multiCallback.loaded(dataEmiter);
  	    }
    	if(singleCallback) {
    	   return run(CallType.LOADED, setting, cb, dataEmiter,"", view);
    	}
    	final LoaderWorker work = manager.getMultiWorker().get(key);
        if (work != null) {
        	return work.loaded(dataEmiter, view);
        }
    	return false;
	}
    
    public boolean failed(LoaderSettings setting,LoaderCallback cb,Failed failed,String uri,ImageView view) {
    	if(multiCallback.cbEnabled()) {
			multiCallback.failed(failed);
   	    }
    	if(singleCallback) {
     	   return run(CallType.FAILED, setting, cb, failed, uri, view);
     	}
    	final LoaderWorker work = manager.getMultiWorker().get(key);
        if (work != null) {
        	return work.failed(failed, uri, view);
        }
    	return false;
	}
    
    public boolean progress(LoaderSettings setting,final ProgressCallback pcb, final int bytes,final int total) {
    	if (pcb == null)
			return false;
		if (singleCallback) {
			Runnable rCallback = new Runnable() {
				@Override public void run() {
					pcb.onProgressUpdate(bytes,total);
				}
			};
			if (setting.getHandler() != null)
				setting.getHandler().post(rCallback);
			else 
				pcb.onProgressUpdate(bytes,total);
     	   return true;
     	}
    	final LoaderWorker work = manager.getMultiWorker().get(key);
        if (work != null) {
        	return work.progress(setting,pcb, bytes, total);
        }
    	return false;
	}
    
    public void clearMultiWorker() {
        manager.getMultiWorker().clear();
	}
	
	private boolean run(final CallType type, LoaderSettings setting, final LoaderCallback cb,final Object object,final String uri,final ImageView view) {
		if (!setting.hasCallback())
			return false;
		Runnable rCallback = new Runnable() {
			@Override public void run() {
				doSingleCallback(cb,type,object,uri,view);
			}
		};
		Handler handler = setting.getHandler();
		if (handler != null) {
			handler.post(rCallback);
			return true;
		} 
		else {
			return doSingleCallback(cb,type,object,uri,view);
		}
	}

	public boolean doSingleCallback(final LoaderCallback cb,final CallType type,Object object,final String uri,final ImageView view) {
		switch (type) {
			case LOADED:
				cb.onSuccess((DataEmiter) object, view);
				break;
			case FAILED:
				cb.onFailed((Failed) object, uri,view);
				break;
			default:
				break;
		}
		return true;
	}

	class Bytes {
		int bytes;
		int total;
		Bytes(int bytes,int total) {
			this.bytes = bytes;
			this.total = total;
		}
	}
 }
