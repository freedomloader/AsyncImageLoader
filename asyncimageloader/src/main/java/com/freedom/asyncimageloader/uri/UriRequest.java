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

import com.freedom.asyncimageloader.uri.URLEncoded.UriScheme;
import android.net.Uri;


public final class UriRequest {
	public Uri uri;
	public final int uridrawable;
	public final Object image;
	public final String name;

	public UriRequest(String uri, int uridrawable) {
		this(Uri.parse(uri),uridrawable);
	}

	public UriRequest(Uri uri, int uridrawable) {
		this(uri,null,uridrawable);
	}
	
	public UriRequest(Object image,String name) {
		this(Uri.parse(name),name,image,0);
	}
	
	public UriRequest(String uri, Object image,int uridrawable) {
		this(Uri.parse(uri),image,uridrawable);
	}
	
	private UriRequest(Uri uri, Object image,int uridrawable) {
		this(uri,null,image,uridrawable);
	}
	
	public UriRequest(Uri uri, String dName,Object image,int uridrawable) {
		this.uri = uri;
		this.name = uri != null ? uri.toString() : dName;
		this.image = image;
		this.uridrawable = uridrawable;
	}

	public UriScheme getSchemeType() {
		return UriScheme.match(UriSubmake.subUriQueryFormat(getUriString()));
	}

	public String getScheme() {
		return image != null ? name : uri != null ? uri.getScheme() : "local://";
	}

	public Uri getUri() {
		return uri;
	}
	
	public int getUriDrawable() {
		return uridrawable;
	}

	public String getUriString() {
		return name;
	}

	public String getUriName() {
		if (getUriString() != null && getUriDrawable() == 0) {
			return getUriString();
		}
		return String.valueOf(getUriDrawable());
	}
	
	@Override
	public String toString() {
		return getUriString();
	}
}
