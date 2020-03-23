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

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.freedom.asyncimageloader.assist.Resizer;
import com.freedom.asyncimageloader.assist.BitmapDecoder.ImageScale;
import com.freedom.asyncimageloader.imagewrapper.ImageWrapper;
import com.freedom.asyncimageloader.listener.LoaderCallback.Adapter;

import static android.widget.ImageView.ScaleType.CENTER_CROP;

/** Callback for CustomImage events. */
public class SimpleViewWrapper  extends Adapter implements ImageWrapper {

	String uri = "";
	ScaleType scaleType = null;

	public SimpleViewWrapper() {
	}
	
	public SimpleViewWrapper(String imageUri) {
		this.uri = imageUri;
	}

	@Override
	public String getUri() {
		// TODO Auto-generated method stub
		return uri.toString(); // Get image URLEncoded
	}

	@Override
	public SimpleViewWrapper putUri(String imageUri) {
		// TODO Auto-generated method stub
		this.uri = imageUri;
		return this;
	}
	
	@Override
	public int getHashCode() {
		return uri.equals("") ? super.hashCode() : uri.hashCode();
	}

	@Override
	public ImageView getView() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isViewNotActive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean doAnimation(Animation animation,String doAnimate) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean hasDrawable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ScaleType getScaleType() {
		// TODO Auto-generated method stub
		return CENTER_CROP;
	}

	@Override
	public ImageScale getViewScaleType() {
		// TODO Auto-generated method stub
		return ImageScale.CENTER;
	}

	@Override
	public boolean onImageBitmap(Bitmap bitmap) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean onImageDrawable(Drawable drawable) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean setScaleType(ScaleType scaleType) {
		// TODO Auto-generated method stub
		this.scaleType = scaleType;
		return true;
	}

	@Override
	public Resizer getTargetSize() {
		// TODO Auto-generated method stub
		return new Resizer(0,0);
	}
}
