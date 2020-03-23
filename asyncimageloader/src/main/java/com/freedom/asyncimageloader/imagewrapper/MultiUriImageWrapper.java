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
package com.freedom.asyncimageloader.imagewrapper;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.freedom.asyncimageloader.AsyncImage;
import com.freedom.asyncimageloader.StringWriter;
import com.freedom.asyncimageloader.AsyncImage.LoaderRequest;

import android.view.View;
import com.freedom.asyncimageloader.uri.*;

public class MultiUriImageWrapper extends SimpleViewWrapper {
	Hashtable<View,String> mPendingMultiLoad = new Hashtable<View,String>();
	List<View> mPendingView;
	List<String> mPendingUri;
	
	public MultiUriImageWrapper(Hashtable<View,String> multi,
			LoaderRequest request) {
		this.mPendingMultiLoad = multi;
		if (mPendingView == null) {
			mPendingView = new ArrayList<View>(10);
		}
		if (mPendingUri == null) {
			mPendingUri = new ArrayList<String>(10);
		}
		for (View view : mPendingMultiLoad.keySet()) {
			mPendingView.add(view);
		}
		for (int i = 0, n = multiViewSize(); i < n; i++) {
			mPendingUri.add(multi.get(mPendingView.get(i)));
		}
		init(request);
	}
	
	private void init(final LoaderRequest request) {
		if (multiUriEnabled()) {
			if (multiUriSize() == multiViewSize()) {
				for (View view : mPendingView) {
					if (request != null) {
						UriRequest uriRequest = new UriRequest(mPendingMultiLoad.get(view), 0);
						request.append(uriRequest);
						request.loadinto(view);
					}
				}
			}
		}
	}

	private boolean multiUriEnabled() {
		return !mPendingMultiLoad.isEmpty() && multiUriSize() != 0;
	}

	public int pendingMultiUriSize() {
		return mPendingMultiLoad.size();
	}

	private int multiUriSize() {
		return mPendingUri.size();
	}

	private int multiViewSize() {
		return mPendingView.size();
	}

	@Override
	public String toString() {
		StringWriter stringAppend = new StringWriter()
				.append(" size: ", getTotalViewUriSize())
				.append(" pending view: ", mPendingView.toString())
				.append(" pending uri: ", mPendingUri.toString())
				// .append(" hash set: ",mPendingMultiLoad.toString())
				.flush();
		return stringAppend.toString();
	}

	private String getTotalViewUriSize() {
		return "views: " + multiViewSize() + " uris: " + multiUriSize();
	}
}
