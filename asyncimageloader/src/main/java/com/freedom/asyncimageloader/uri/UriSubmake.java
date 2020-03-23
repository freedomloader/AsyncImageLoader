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
package com.freedom.asyncimageloader.uri;

import java.io.File;

import com.freedom.asyncimageloader.uri.URLEncoded.URIValue;
import com.freedom.asyncimageloader.uri.URLEncoded.UriScheme;


public enum UriSubmake {
	FORMAT("format"), SIZE("size"), TIME("time");
	 
	private String type;

	UriSubmake(String uriType) {
		this.type = uriType;
	}

	public FormData form(String uri) {
		switch (match(uri)) {
		case FORMAT:
			return new FormData(FORMAT, removeQuery(uri));
		case TIME:
			return new FormData(TIME, removeQuery(uri));
		case SIZE:
			return new FormData(SIZE, removeQuery(uri));
		}
		return null;
	}

	private static UriSubmake match(String uri) {
		if (uri != null) {
			for (UriSubmake usm : values()) {
				if (usm.getQueryParameter(uri)) {
					return usm;
				}
		}}
		return null;
	}

	private boolean getQueryParameter(String uri) {
		URLEncoded query = URLEncoded.parse(uri);
		return query.contains(type);
	}

	public String removeQuery(String uri) {
		URLEncoded query = URLEncoded.parse(uri);
		if (uri == null) {
			return null;
		}
		query.remove(type);
		return query.getURL(URIValue.GET_AUTHORITY);
	}

	public void addQuery(String uri, String value) {
		URLEncoded query = URLEncoded.parse(uri);
		query.add(type,value);
	}

	public static File subUriQueryFormat(File uri) {
		return new File(subUriQueryFormat(uri.getAbsolutePath()));
	}
	
	public static boolean isThumbFormat(String uri) {
		return subUriQueryFormat(uri).startsWith("thumb") ? true : false;
    }
	
	public static String subUriQueryFormat(String uri) {
 	 String thumb = UriScheme.THUMB.drag(uri);
 	 	
  	 return (uri.endsWith(".3gp") || uri.endsWith(".mp4")
			 || uri.endsWith(".avi") || uri.endsWith(".flv")) ? thumb : uri;
  	}

	public String getType() {
		return type;
	}

	/** remove end .format from uri */
	public static String removeEnd(String uri) {
		if (uri == null) {
	        return uri;
	    }
		if (uri.endsWith(UriUtil.JPG_EXTENSION)) {
			return uri.substring(0, UriUtil.JPG_EXTENSION.length()+1);
		}else if (uri.endsWith(UriUtil.PNG_EXTENSION)) {
			return uri.substring(0, UriUtil.PNG_EXTENSION.length()+1);
		}else if (uri.endsWith(UriUtil.BMP_EXTENSION)) {
			return uri.substring(0, UriUtil.BMP_EXTENSION.length()+1);
		}else if (uri.endsWith(UriUtil.GIF_EXTENSION)) {
			return uri.substring(0, UriUtil.GIF_EXTENSION.length()+1);
		}else if (uri.endsWith(UriUtil.FREEDOM_EXTENSION)) {
			return uri.substring(0, UriUtil.FREEDOM_EXTENSION.length()+1);
		}else {
			return uri;
		}
	}
	
	class FormData {
		UriSubmake sub;
		String uri;

		FormData(UriSubmake sub, String uri) {
			this.sub = sub;
			this.uri = uri;
		}
	}
}