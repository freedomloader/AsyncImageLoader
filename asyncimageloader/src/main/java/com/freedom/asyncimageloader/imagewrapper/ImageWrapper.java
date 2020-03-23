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

import com.freedom.asyncimageloader.assist.Resizer;
import com.freedom.asyncimageloader.assist.BitmapDecoder.ImageScale;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public interface ImageWrapper {
	Resizer getTargetSize();

	String getUri();
	
	ImageWrapper putUri(String imageUri);

	int getHashCode();

	ImageView getView();

	boolean isViewNotActive();

	boolean doAnimation(Animation animation,String doAnimate);
	
	boolean hasDrawable();
	
	ScaleType getScaleType();
	
	ImageScale getViewScaleType();
	
	boolean onImageBitmap(Bitmap bitmap);

	boolean onImageDrawable(Drawable drawable);

	boolean setScaleType(ScaleType scaleType);
}
