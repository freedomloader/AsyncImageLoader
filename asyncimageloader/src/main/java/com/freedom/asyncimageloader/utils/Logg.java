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
package com.freedom.asyncimageloader.utils;

import android.util.Log;

public class Logg {

	public static String TAG = "Async-Image";

	public static void info(String msg) {
		String logtext = getLog(null,msg);
		Log.println(Log.INFO, TAG, logtext);
	}

	public static void warning(String msg) {
		String logtext = getLog(null,msg);
		Log.println(Log.WARN, TAG, logtext);
	}
	
	public static void debug(String msg) {
		String logtext = getLog(null,msg);
		Log.println(Log.DEBUG, TAG, logtext);
	}
	 
	public static void error(Throwable e,String msg) {
		String logtext = getLog(e,msg);
		Log.println(Log.ERROR, TAG, logtext);
	}
	
	private static String getLog(Throwable ex, String message) {
		String logText;
		if (ex == null) {
			logText = message;
		} else {
			String logMsg = message == null ? ex.getMessage() : message;
			String logBody = Log.getStackTraceString(ex);
			logText = String.format("%1$s\n%2$s", logMsg, logBody);
		}
		return logText;
	}
}