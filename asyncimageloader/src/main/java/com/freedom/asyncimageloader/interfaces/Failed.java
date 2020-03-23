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

import com.freedom.asyncimageloader.StringWriter;

/**
 * load failed reason why the loading image and displaying was failed
 */
public class Failed {

	/** the reason type why the image failed to load */
	public static enum FailMethod {
		/** when decoding bitmap failed .*/
		IMAGE_DECODE_FAILED,
		/** when downloading service error .*/
		SERVICE_FAILED,
		/** request was denied unknown denied .*/
		REQUEST_DENIED,
		/** memory cache was out of memory when loading bitmap .*/
		OUT_OF_MEMORY,
		/** when task interrupted .*/
		INTERRUPTED,
		/** when image view reused for another task .*/
		REUSED,
		/** return both interrupted and reused .*/
		INTERRUPTED_REUSED,
		/** when image view not active .*/
		VIEW_UNACTIVE,
		/** when image view not active .*/
		URL_INVALID,
		/** unknown reason .*/
		UNKNOWN_ERROR
		}

	/** type of fail method */
	private final FailMethod type;
	/** message mean the reason why the error occurred .*/
    private final String message;

	public Failed(FailMethod type,String message) {
		this.type = type;
		this.message = message;
	}

	/** @return {@see Failed type} */
	public FailMethod getType() {
		/** return type .*/
		return type;
	}

	/** @return {@see getMessage} */
	public String getMessage() {
		/** return message .*/
		return message;
	}

	@Override
	public String toString() {
		StringWriter stringAppend = new StringWriter()
			.append(" type: ",getType())
			.append(" message: ",getMessage())
			.append(" failed: ","failed with call back")
			.flush();
		return stringAppend.toString();
	}
}
