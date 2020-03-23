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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import com.freedom.asyncimageloader.interfaces.PaintDrawable;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
@SuppressLint("NewApi")
public class MultiViewWrapper extends ImageViewWrapper {
	List<Reference<View>> multiImageView;
	protected Reference<View> viewReference;
	
	public MultiViewWrapper(String uri,View view1,View view2,View view3) {
		super(null,uri);
		this.multiImageView = new ArrayList<Reference<View>>(3);

		if(view1 != null) {
		   multiImageView.add(new WeakReference<View>(view1));
		}
		if(view2 != null) {
		   multiImageView.add(new WeakReference<View>(view2));
		}
		if(view3 != null) {
		   multiImageView.add(new WeakReference<View>(view3));
		}
		for(Reference<View> views:  multiImageView) {
			if(viewReference == null || viewReference.get() == null) {
			   viewReference = views;
			}
	    }
	}
	
	public MultiViewWrapper(String uri,List<Reference<View>> multiImageView) {
		super(null,uri);
		this.multiImageView = multiImageView;
		for (Reference<View> views:  multiImageView) {
			if(viewReference == null || viewReference.get() == null) {
			   viewReference = views;
			}
	    }
	}

	@Override
	public ImageView getView() {
		if(viewReference.get() instanceof ImageView) {
		   return (ImageView) viewReference.get();
		} else {
		   return null;
		}
	}
	
	@Override
	public boolean isViewNotActive() {
		return viewReference.get() == null;
	}
	
	@Override
	public boolean setScaleType(ScaleType scaleType) {
		View view = getView();
		if (view instanceof ImageView && this.scaleType == null 
			&& scaleType != null && view != null) { 
			for(int i = 0, n = multiImageViewSize(); i < n; i++) {
				Reference<View> views = multiImageView.get(i);
				if(views.get() instanceof ImageView) {
				   ((ImageView) views.get()).setScaleType(scaleType);
				}
				this.scaleType = scaleType;
		    }
			return true;
	    }
		return false;
	}
	
	@Override
	public boolean onImageBitmap(Bitmap bitmap) {
		if (Looper.myLooper() == Looper.getMainLooper() && multiImageView != null) { 
			for(int i = 0, n = multiImageViewSize(); i < n; i++) {
				Reference<View> views = multiImageView.get(i);
				if(views.get() instanceof ImageView) {
				   ((ImageView) views.get()).setImageBitmap(bitmap);
				} else if(views.get() instanceof View) {
					views.get().setBackground(new PaintDrawable(bitmap, 1));
				}
		    }
			return true;
	    }
		return false;
	}
	
	@Override
	public boolean onImageDrawable(Drawable drawable) {
		if (Looper.myLooper() == Looper.getMainLooper() && multiImageView != null) { 
			for(int i = 0, n = multiImageViewSize(); i < n; i++) {
				Reference<View> views = multiImageView.get(i);
				if(views.get() instanceof ImageView) {
					((ImageView) views.get()).setImageDrawable(drawable);
				} else if(views.get() instanceof View) {
					views.get().setBackground(drawable);
				}
		    }
			return true;
		}
		return false;
	}
	
	private int multiImageViewSize() {
		return multiImageView.size();
	}
}