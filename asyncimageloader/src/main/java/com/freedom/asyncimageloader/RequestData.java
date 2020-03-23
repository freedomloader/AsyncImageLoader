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

import java.util.concurrent.locks.ReentrantLock;

import com.freedom.asyncimageloader.interfaces.DataEmiter;
import com.freedom.asyncimageloader.assist.Resizer;
import com.freedom.asyncimageloader.imagewrapper.ImageWrapper;
import com.freedom.asyncimageloader.interfaces.SocketResponse;
import com.freedom.asyncimageloader.uri.UriRequest;

public final class RequestData {
	final ImageWrapper imageWrapper;
    public final LoaderSettings setting;
    public final ImageOptions imageOptions;
    public final Resizer size;
    public final DataEmiter dataEmiter;
    final UriRequest uriRequest;
    final String memoryCacheKey;
	
	final ReentrantLock checkLockUri;
	final Resizer targetSize;
	
    public RequestData(UriRequest uriRequest, ImageWrapper wrapper,Resizer targetSize,DataEmiter dataEmiter
    		,LoaderSettings settings,String memoryCacheKey,
    	ReentrantLock lockUri) {
    	this.setting = settings;
        this.uriRequest = uriRequest;
        this.targetSize = targetSize;
        this.memoryCacheKey = memoryCacheKey;
        this.imageWrapper = wrapper;
        this.imageOptions = settings.getImageOptions();
        this.size = settings.getSize();
        this.dataEmiter = dataEmiter;
        this.checkLockUri = lockUri;
    }
    
    public ImageWrapper getImageWrapper() {
    	return imageWrapper;
    }
    
    public String getMemoryCacheKey() {
    	return memoryCacheKey;
    }
    
    public LoaderSettings getSetting() {
    	return setting;
    }
    
    public SocketResponse getFailed(String text) {
    	return new SocketResponse(0,uriRequest.getUriString(), text);
    }
    
    public UriRequest getUriRequest() {
    	return uriRequest;
    }
    
    public SocketResponse getFailed() {
    	return new SocketResponse(0,uriRequest.getUriString(), "loading failed with uri: "+uriRequest.getUriString());
    }
    
    @Override
	public String toString() {
		StringWriter append = new StringWriter()
	    .append(" loading uri: ",uriRequest.getUriString())
	    .append(" image size: ",setting.getSize())
	    .append(" image options: ",imageOptions)
	    .append(" memoryCacheKey: ",getMemoryCacheKey())
	    .append(" memory cache: ",setting.options.memoryCache.toString())
	    .flush();
		return append.toString();
	 }
}
