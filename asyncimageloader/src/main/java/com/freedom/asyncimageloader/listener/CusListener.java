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

public class CusListener {

	/** Bytes Listener */
	public interface BytesListener {
		/**
		 * Bytes Listener to update bytes when reading from input stream
	     * 
	     * @param current current loaded bytes
	     * @param total available size of downloading file
	     * 
	     * if true continue loading - if false stop reading file bytes
	     * <p>This method can only be invoked when loading bytes.</p>
	     * 
		 * @return <tt>true</tt> - if downloading should continue; <tt>false</tt> - stop reading file bytes
		 */
		boolean onBytesUpdate(int current, int total);
	}
	
	/** failed Listener */
	public interface failedListener {
		/**
		 * @param url failed URLEncoded
		 * @return <b>true</tt> if URL is block from loading till loader start again
		 */
		boolean onFailed(String url);
	}
	
	/** Listener Time */
	public interface TimeListener {
		/**
		 * @param uri end time URLEncoded
		 * @return <tt>true</tt> - if URL should stop; <tt>false</tt> - continue loading
		 */
		boolean onTime(String uri);
	}
}
