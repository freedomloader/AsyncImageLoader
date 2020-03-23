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

import static android.widget.ImageView.ScaleType.CENTER_CROP;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import com.freedom.asyncimageloader.assist.Resizer;
import com.freedom.asyncimageloader.assist.BitmapDecoder.ImageScale;
import com.freedom.asyncimageloader.utils.Logg;

public class ImageViewWrapper implements ImageWrapper {

	protected String SCALE_TYPE_IS_NULL = "Scale Type is null set default scale type using " +
	 		"new LoaderOptions().setDefaultScaleType(scaleType) inside Application .";
	protected String IMAGE_VIEW_IS_NULL = "ImageWrapper must not be null for any reason.";
	
	String uri;
	protected Reference<ImageView> imageViewReference;
	ScaleType scaleType = null;
	private final int empty_size = 0;

	public ImageViewWrapper(ImageView imageView,String uri) {
		this.uri = uri;
		this.imageViewReference = new WeakReference<ImageView>(imageView);
	}

	@Override
	public String getUri() {
		return uri; // Get image URLEncoded
	}
	
	@Override
	public ImageViewWrapper putUri(String imageUri) {
		this.uri = imageUri; // Put image URLEncoded
		return this;
	}
	
	@Override
	public Resizer getTargetSize() {
		return new Resizer(getWidth(),getHeight());
	}
	
	public int getWidth() {
		ImageView iv = getView();
	    int width = empty_size;
		if (iv != null) {
		    width = iv.getMeasuredWidth();
		}
		return width;
	}

	public int getHeight() {
		ImageView iv = getView();
		int height = empty_size;
		if (iv != null) {
			height = iv.getMeasuredHeight();
		}
		return height;
	}
	
	@Override
	public boolean setScaleType(ScaleType scaleType) {
		ImageView imageView = getView();
		if(this.scaleType != null) {
			return true;
		}
		
		if(scaleType == null) 
			Logg.debug(SCALE_TYPE_IS_NULL);
		
		if(imageView == null) 
			Logg.debug(IMAGE_VIEW_IS_NULL);
		
		if (imageView != null && scaleType != null) { 
			imageView.setScaleType(scaleType); // Set Scale
		    this.scaleType = scaleType;
			return true;
		}
		return false;
	}
	
	@Override
	public ImageView getView() {
		return imageViewReference != null ? 
				imageViewReference.get() : null; // Get imageView
	}

	@Override
	public int getHashCode() {
		ImageView imageView = getView();
		return imageView == null ? super.hashCode() : imageView.hashCode();
	}
	
	@Override
	public boolean hasDrawable() {
		return getView() != null; // Check if imageView has drawable
	}
	
	@Override
	public ImageScale getViewScaleType() {
		return getView() == null ? ImageScale.CENTER : 
			ImageScale.scaleView(getView()); // Get Scale Type
	}
	
	@Override
	public ScaleType getScaleType() {
		return scaleType == null ? 
				CENTER_CROP : scaleType; // Get Scale Type
	}
	
	@Override
	public boolean isViewNotActive() {
		return getView() == null; // Check if imageView is null
	}
	
	@Override
	public boolean doAnimation(Animation animation,String doAnimate) {
		ImageView imageView = getView();
		try {
		 if (imageView != null) {
			 if(doAnimate.equals("start")) {
				 imageView.startAnimation(animation);
			 }else {
				 imageView.clearAnimation(); // clear animation
			 }
		 }
		}catch (Exception ex) {
			return false;
		}
		return true;
	}
	
	@Override
	public boolean onImageBitmap(Bitmap bitmap) {
		if (Looper.myLooper() == Looper.getMainLooper()) { 
			ImageView imageView = getView();
			if (imageView != null) {
				imageView.setImageBitmap(bitmap); // Set bitmap image
				return true;
			}
	    }
		return false;
	}
	
	@Override
	public boolean onImageDrawable(Drawable drawable) {
		if (Looper.myLooper() == Looper.getMainLooper()) {
			ImageView imageView = getView();
			if (imageView != null) {
				imageView.setImageDrawable(drawable); // Set Drawable image
				return true;
			}
		}
		return false;
	}
	
}
