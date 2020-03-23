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

import android.view.View;
import android.view.ViewTreeObserver;
import java.lang.ref.WeakReference;
import android.widget.ImageView;

import com.freedom.asyncimageloader.AsyncImage.LoaderRequest;
import com.freedom.asyncimageloader.listener.LoaderCallback;

public class ViewTargetWrapper implements ViewTreeObserver.OnPreDrawListener {
	final LoaderRequest request;
	final WeakReference<ImageView> imageViewRef;
	final WeakReference<View> viewRef;
	LoaderCallback callback;

	public ViewTargetWrapper(LoaderRequest request,LoaderCallback callback, View viewRef) {
		if (callback != null) {
			this.callback = callback;
		}
		this.request = request;

		this.viewRef = new WeakReference<View>(viewRef);
		this.imageViewRef = null;
		viewRef.getViewTreeObserver().addOnPreDrawListener(this);
	}
	
	public ViewTargetWrapper(LoaderRequest request, LoaderCallback callback, ImageView imageViewRef) {
		if (callback != null) {
			this.callback = callback;
		}
		this.request = request;

		this.viewRef = null;
		this.imageViewRef = new WeakReference<ImageView>(imageViewRef);
		imageViewRef.getViewTreeObserver().addOnPreDrawListener(this);
	}

	public void stopRequest() {
		callback = null;
		View mViewRef = this.imageViewRef != null ? this.imageViewRef.get() : 
			this.viewRef != null ? this.viewRef.get() : null;
			
		if (mViewRef == null) {
			return;
		}
		ViewTreeObserver vto = mViewRef.getViewTreeObserver();
	    if (!vto.isAlive()) {
	      return;
	    }
	    vto.removeOnPreDrawListener(this);
	}
	
	@Override
	public boolean onPreDraw() {
		View mViewRef = this.imageViewRef != null ? this.imageViewRef.get() : 
			this.viewRef != null ? this.viewRef.get() : null;
			
		if (mViewRef == null) {
			return true;
		}
		
		ViewTreeObserver viewTree = mViewRef.getViewTreeObserver();
		if (!viewTree.isAlive()) {
			return true;
		}
		int measuredWidth = mViewRef.getMeasuredWidth();
		int measuredHeight = mViewRef.getMeasuredHeight();

		if (measuredWidth <= 0 || measuredHeight <= 0) {
			return true;
		}
		viewTree.removeOnPreDrawListener(this);

		this.request.unfit()
				.resize(measuredWidth, measuredHeight);
		
		if (callback != null) {
			request.callback(callback);
		}
		
		ImageView imageView = null;
		if (mViewRef instanceof ImageView)
			imageView = (ImageView) mViewRef;
		
		if (imageView != null) {
		    request.loadinto(imageView);
		} else {
		    request.loadinto(mViewRef);
		}
		return true;
	}
}