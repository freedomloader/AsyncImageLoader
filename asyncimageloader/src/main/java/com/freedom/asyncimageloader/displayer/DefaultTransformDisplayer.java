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
package com.freedom.asyncimageloader.displayer;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.freedom.asyncimageloader.interfaces.DataEmiter;
import com.freedom.asyncimageloader.displayer.TransformDisplayer;
import com.freedom.asyncimageloader.imagewrapper.ImageWrapper;
import com.freedom.asyncimageloader.interfaces.PaintDrawable;
import com.freedom.asyncimageloader.*;
import android.graphics.*;
import com.freedom.asyncimageloader.utils.*;
import com.freedom.asyncimageloader.assist.*;	

public class DefaultTransformDisplayer implements TransformDisplayer {

	@Override
	public Bitmap onLoaded(LoaderSettings request,DataEmiter dataEmiter, ImageWrapper imageWrapper,
			int rounded) {
		if (imageWrapper == null)
			throw new IllegalArgumentException("ImageWrapper can not be null.");

		if (rounded != 0) {
			if (request.shouldMargeBitmap())
				imageWrapper.onImageBitmap(dataEmiter.bitmap);
           else
           	    imageWrapper.onImageDrawable(new PaintDrawable(dataEmiter.bitmap,rounded));
		} else {
			imageWrapper.onImageBitmap(dataEmiter.bitmap);
		}
		return null;
	}

	@Override
	public void placeHolder(LoaderSettings request,ImageWrapper imageWrapper, Drawable holderDrawable) {
		if (request.transformPlaceHolder() && holderDrawable != null) {
			Bitmap b = BitmapUtils.drawableToBitmap(holderDrawable);
			imageWrapper.onImageDrawable(new PaintDrawable(b, request.getImageOptions().rounded));
		} else {
		    imageWrapper.onImageDrawable(holderDrawable);
		}
	}

	@Override
	public void failed(LoaderSettings request,ImageWrapper imageWrapper, Drawable failedDrawable) {
		ImageView view = imageWrapper.getView();
		if (view != null) {
			if (request.transformFailedHolder() && failedDrawable != null) {
				Bitmap b = BitmapUtils.drawableToBitmap(failedDrawable);
				imageWrapper.onImageDrawable(new PaintDrawable(b, request.getImageOptions().rounded));
			} else {
			    imageWrapper.onImageDrawable(failedDrawable);
			}
		}
	}
	
	@Override
	public Bitmap transformBitmap(Resizer resizer,Bitmap bitmap, LoaderSettings request) {
		int rounded = request.getImageOptions().rounded;
		if (rounded != 0) {
			//Resizer resizer = request.getTargetImageSize();	
			Bitmap copybitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
			//Canvas canvas = new Canvas(bitmap);
			Bitmap roundBitmap = BitmapUtils.toRoundedCroppedBitmap(copybitmap, rounded);
			//canvas.drawBitmap(roundBitmap, 0, 0, null);
			return roundBitmap;
		} else {

		}
		return bitmap;
	}
	
	@Override
	public String transformKey() {
		// TODO Auto-generated method stub
		return "default-displayer";
 }
}
