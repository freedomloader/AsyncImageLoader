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

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import com.freedom.asyncimageloader.utils.TimeUtils;

import android.content.Context;

public class UriControlBuilder {
	public Context context;

	final Map<String, Boolean> cacheKeysForBlockUri = Collections.synchronizedMap(new WeakHashMap<String, Boolean>());
	final Map<String, Integer> cacheKeysForDelayUri = Collections.synchronizedMap(new WeakHashMap<String, Integer>());
	final Map<String, String> cacheKeysForChangeUri = Collections.synchronizedMap(new WeakHashMap<String, String>());
	private static int DEFAULT_MILLS = TimeUtils.SECOND.TEN_SECOND;

	int delayAllUri = 0;
	int blockHolder = 0;
	int delayHolder = 0;

	public UriControlBuilder() { }

	/** Set delay URLEncoded */
	public UriControlBuilder setDelayUri(String uri) {
		cacheKeysForDelayUri.put(uri, DEFAULT_MILLS);
		return this;
	}

	/** Set delay URLEncoded with mills */
	public UriControlBuilder setDelayUri(String uri, int millis) {
		cacheKeysForDelayUri.put(uri, millis);
		return this;
	}

	/** delay ALL URLEncoded with mills */
	public UriControlBuilder delayAllUri(int millis) {
		delayAllUri = millis;
		return this;
	}

	/** Set Block URLEncoded */
	public UriControlBuilder setBlockUri(String uri) {
		cacheKeysForBlockUri.put(uri, true);
		return this;
	}
	
	/** Set Block Ends With URLEncoded */
	public UriControlBuilder blockUriEndsWith(String end) {
		cacheKeysForBlockUri.put(end, false);
		return this;
	}

	/** Set Change Uri */
	public UriControlBuilder changeUri(String from, String to) {
		cacheKeysForChangeUri.put(from, to);
		return this;
	}

	/** Set Block URLEncoded holder */
	public UriControlBuilder holderForBlockUri(int drawable) {
		this.blockHolder = drawable;
		return this;
	}

	/** Set Delay URLEncoded holder */
	public UriControlBuilder holderForDelayUri(int drawable) {
		this.delayHolder = drawable;
		return this;
	}
}
