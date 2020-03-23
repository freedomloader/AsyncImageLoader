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
package com.freedom.asyncimageloader.interfaces;

import com.freedom.asyncimageloader.listener.SocketCallBack;

public class HttpSocket {

	public static final int DEFAULT_READ_TIMEOUT = 20 * 1000;
	public static final int DEFAULT_CONNECT_TIMEOUT = 5 * 1000;
	private static final boolean DEFAULT_USE_CACHES = false;
	private static final boolean DEFAULT_FOLLOW_REDIRECTS = true;
	public static final String DEFAULT_USE_REQURST_METHOD = "GET";
	public static final int DEFAULT_ERROR_CODE = 404;
	
	public int errorCode;
	public String error_404_url;
	public String requestMethod;
	public boolean instanceFollowRedirects;
	public boolean followRedirects;
	public int readtimeout;
	public int connecttimeout;
	public boolean useCaches;
	public SocketCallBack callback;

	public HttpSocket(SocketConnection builder) {
		instanceFollowRedirects = builder.instanceFollowRedirects;
		followRedirects = builder.followRedirects;
		error_404_url = builder.error_404_url;
		errorCode = builder.errorCode;
		requestMethod = builder.requestMethod;
		readtimeout = builder.readtimeout;
		connecttimeout = builder.connecttimeout;
		useCaches = builder.useCaches;
		callback= builder.callback;
	}

	public String getRequestMethod() {
		// TODO: Implement this method
		return requestMethod;
	}

	public boolean getFollowRedirects(){
		// TODO: Implement this method
		return followRedirects;
	}

	public int getErrorCode() {
		return errorCode;
	}
	
	public String getError404Url() {
		return error_404_url;
	}
	
	public int getConnectTimeout() {
		return connecttimeout;
	}

	public int getReadTimeout() {
		return readtimeout;
	}

	public boolean getInstanceFollowRedirects() {
		return instanceFollowRedirects;
	}

	public boolean getUseCaches() {
		return useCaches;
	}
 
	 public static class SocketConnection {
	 private String error_404_url = null;
	 private int errorCode = 0;
	 private String requestMethod = null;
	 private  boolean instanceFollowRedirects = false;
	 private boolean followRedirects = DEFAULT_FOLLOW_REDIRECTS;
	 private  int readtimeout = 0;
	 private int connecttimeout = 0;
	 private boolean useCaches = false;
	 private SocketCallBack callback;
	 
	    public SocketConnection(){}
	    
	 	/**  Set Connect Timeout */
	 	public SocketConnection setConnectTimeout(int timeout) {
	 		this.connecttimeout = timeout;
	 		return this;
	 	}
	 	
	 	/**  Set Read Timeout */
	 	public SocketConnection setReadTimeout(int timeout) {
	 		this.readtimeout = timeout;
	 		return this;
	 	}
	 	
		public SocketConnection setErrorCode(int code) {
			errorCode = code;
			return this;
		}
		
		public SocketConnection setError404Url(String errorurl) {
			error_404_url = errorurl;
			return this;
		}
		
		public SocketConnection setRequestMethod(String method) {
			requestMethod = method;
			return this;
		}
		
	 	/**  Set InstanceFollowRedirects */
	 	public SocketConnection setInstanceFollowRedirects(boolean read) {
	 		this.instanceFollowRedirects = read;
	 		return this;
	 	}
	 	
		/**  Set FollowRedirects */
	 	public SocketConnection setFollowRedirects(boolean follow) {
	 		this.followRedirects = follow;
	 		return this;
	 	}
		
	 	/**  Set Use Caches */
	 	public SocketConnection setUseCaches(boolean useCaches) {
	 		this.useCaches = useCaches;
	 		return this;
	 	}
	 	
	 	/**  Set Call back */
	 	public SocketConnection setCallback(SocketCallBack callback) {
	 		this.callback = callback;
	 		return this;
	 	}
	 	
	 	public HttpSocket build() {
            if (connecttimeout == 0) {
        	     connecttimeout = HttpSocket.DEFAULT_CONNECT_TIMEOUT;
            }
            if (readtimeout == 0) {
        	     readtimeout = HttpSocket.DEFAULT_READ_TIMEOUT;
            }
            if (useCaches == false) {
        	     useCaches = DEFAULT_USE_CACHES;
            }
			if (requestMethod == null) {
				requestMethod = DEFAULT_USE_REQURST_METHOD;
			}
			if (errorCode == 0) {
				errorCode = DEFAULT_ERROR_CODE;
			}
	 		return new HttpSocket(this);
	 	}}
  
}
