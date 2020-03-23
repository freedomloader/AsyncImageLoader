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

import com.freedom.asyncimageloader.interfaces.DataEmiter;
import com.freedom.asyncimageloader.imagewrapper.ImageWrapper;
import com.freedom.asyncimageloader.AsyncImage.LoaderRequest;

import android.graphics.drawable.Drawable;
import com.freedom.asyncimageloader.*;
import android.graphics.*;
import com.freedom.asyncimageloader.assist.*;
import android.widget.*;
import android.view.*;
import android.view.ViewTreeObserver.*;
import com.freedom.asyncimageloader.utils.*;
import java.util.*;
public interface TransformDisplayer {

	Bitmap onLoaded(LoaderSettings request,DataEmiter dataEmiter, ImageWrapper view,int roundedCorner);
	void placeHolder(LoaderSettings request,ImageWrapper imageWrapper,Drawable holderDrawable);
	void failed(LoaderSettings request,ImageWrapper imageWrapper,Drawable failedDrawable);
	Bitmap transformBitmap(Resizer resizer,Bitmap bitmap,LoaderSettings request);
	String transformKey();

	public static class EmptyDisplayer implements TransformDisplayer {

		@Override
		public Bitmap onLoaded(LoaderSettings request,DataEmiter dataEmiter, ImageWrapper imageWrapper, int roundedCorner) {
		     return loadBitmapTask(request,dataEmiter,imageWrapper,roundedCorner);
		}

		@Override
		public void placeHolder(LoaderSettings request,ImageWrapper imageWrapper,Drawable holderDrawable) {
			if (imageWrapper == null)
				throw new IllegalArgumentException("ImageWrapper can not be null.");

			if (request.transformPlaceHolder() && holderDrawable != null) {
				Bitmap b = BitmapUtils.drawableToBitmap(holderDrawable);
				Bitmap roundBitmap = transformBitmap(request.getSize(),b,request);
				imageWrapper.onImageBitmap(roundBitmap);
			} 
			else if (holderDrawable != null) {
				imageWrapper.onImageDrawable(holderDrawable);
			}
		}

		@Override
		public void failed(LoaderSettings request,ImageWrapper imageWrapper, Drawable failedDrawable) {
			if (imageWrapper == null)
				throw new IllegalArgumentException("ImageWrapper can not be null.");

			ImageView view = imageWrapper.getView();
			if (view != null) {
				if (request.transformFailedHolder() && failedDrawable != null) {
					Bitmap b = BitmapUtils.drawableToBitmap(failedDrawable);
					Bitmap roundBitmap = transformBitmap(request.getSize(),b,request);;
					imageWrapper.onImageBitmap(roundBitmap);
				} 
				else if(failedDrawable != null) {
					imageWrapper.onImageDrawable(failedDrawable);
				}
			}
		}

		@Override
		public Bitmap transformBitmap(Resizer resizer,Bitmap bitmap, LoaderSettings request) {
			return bitmap;
		}

		@Override
		public String transformKey() {
			return "empty-displayer";
		}
		
		public Bitmap loadBitmapTask(final LoaderSettings request,final DataEmiter dataEmiter, final ImageWrapper imageWrapper
		,int roundedCorner) {
			if (imageWrapper == null)
				throw new IllegalArgumentException("ImageWrapper can not be null.");

			Bitmap roundBitmap = request.shouldMargeBitmap() ? dataEmiter.bitmap : transformBitmap(request.getSize(),dataEmiter.bitmap,request);
			if (roundBitmap == null) {
				if (dataEmiter.bitmap == null)
					return null;
					
				final ImageView v = imageWrapper.getView();
				ViewTreeObserver vto = v.getViewTreeObserver();
				vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
						@Override
						public void onGlobalLayout() {
							v.getViewTreeObserver().removeOnGlobalLayoutListener(this);
							v.post(new Runnable() {
									@Override
									public void run() {
										Bitmap roundBitmap = request.shouldMargeBitmap() ? dataEmiter.bitmap : transformBitmap(new Resizer(v.getWidth(),v.getHeight()),dataEmiter.bitmap,request);
										imageWrapper.onImageBitmap(roundBitmap);
									}
								});			
						}
					});
			}
			else {
				imageWrapper.onImageBitmap(roundBitmap);
			}
			return roundBitmap;
		}

		public boolean isCacheBitmapTransform(LoaderSettings request) {
			return request.options.isMemoryCacheBitmapTransform();
		}

		public boolean isDiscCacheBitmapTransform(LoaderSettings request) {
			return request.options.isDiscCacheBitmapTransform();
		}
	}
}
