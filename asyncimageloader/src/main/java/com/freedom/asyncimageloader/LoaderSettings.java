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

import java.lang.ref.WeakReference;

import com.freedom.asyncimageloader.assist.Resizer;
import com.freedom.asyncimageloader.transform.Transform;
import com.freedom.asyncimageloader.displayer.TransformDisplayer;
import com.freedom.asyncimageloader.disc.LruDiscCache;
import com.freedom.asyncimageloader.imagewrapper.ImageWrapper;
import com.freedom.asyncimageloader.interfaces.AnimationStyle;
import com.freedom.asyncimageloader.listener.LoaderCallback;
import com.freedom.asyncimageloader.displayer.DefaultTransformDisplayer;
import com.freedom.asyncimageloader.uri.UriRequest;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView.ScaleType;
import com.freedom.asyncimageloader.callback.*;
import com.freedom.asyncimageloader.assist.*;
import android.widget.*;

public final class LoaderSettings {

	final Context context;
	public final WeakReference<LoaderCallback> callback;	
    public final WeakReference<ProgressCallback> progressCallback;
	
	final boolean transformPlaceHolder;
	final boolean transformFailedHolder;
	final boolean transformHolder;
	
	final boolean useFindMemCacheAsHolder;
    final boolean cacheInMemory;
    final boolean cacheOnDisc;
	final boolean skipMemoryCache;
	final boolean skipDiscCache;
    final boolean saveThumbnailOnDisc;

    final ImageWrapper imageWrapper;
    final ImageOptions imageOptions;
    Resizer size;
    final UriRequest loaduri;
    final int placeHolderRes; 
    final boolean useAsyncTask; 
	final BitmapMarge margeBitmap;
    final int errorRes; 
    final Drawable placeHolderDrawable;
    final Drawable errorDrawable;
    final ScaleType scaleType;
    final Handler handler;
	final View progressView;
	final String placeHolderUrl;
	final String defineActivityName;
	final boolean resetView;
	final boolean loggingEnabled;
	public final TransformDisplayer transformDisplayer;
	public final TransformDisplayer cachetransformDisplayer;
	public final Transform transform;
	public final LoaderOptions options;

 	public final Bitmap.Config bitmapConfig;
 	public final Options decodingOptions;
	
    public LoaderSettings(final AsyncImage.LoaderRequest request) {
    	options = request.options;
    	size = request.size;
    	imageWrapper = request.imageWrapper;
    	placeHolderRes = request.placeHolderRes;
    	errorRes = request.errorRes;
		placeHolderDrawable = request.placeHolderDrawable;
		errorDrawable = request.errorDrawable;
    	loaduri = request.loaduri;
    	loggingEnabled = request.options.loggingEnabled;
        context = request.options.context;
    	imageOptions = request.imageOptions;
    	scaleType = request.scaleType;
    	callback = request.loadingCallback;
    	progressCallback = request.progressCallback;
		margeBitmap = request.marge;
    	resetView = request.resetView;
    	transformDisplayer = request.transformDisplayer;
		if (request.options != null && request.options.isCacheBitmapTransform())
			cachetransformDisplayer = new DefaultTransformDisplayer();
		else
			cachetransformDisplayer = null;
		transformHolder = request.transformHolder;
		transformPlaceHolder = request.transformHolder;
		transformFailedHolder = request.transformHolder;
		useFindMemCacheAsHolder = request.useFindMemCacheAsHolder;
    	cacheInMemory = request.cacheInMemory;
    	cacheOnDisc = request.cacheOnDisc;
    	skipMemoryCache = request.skipMemoryCache;
    	useAsyncTask = request.useAsyncTask;
    	skipDiscCache = request.skipDiscCache;
    	saveThumbnailOnDisc = request.saveThumbnailOnDisc;
    	handler = request.handler;
    	bitmapConfig = request.bitmapConfig;
    	decodingOptions = request.decodingOptions;
		progressView = request.progressView;
		defineActivityName = request.defineActivityName;
		placeHolderUrl = request.placeHolderUrl;
		transform = request.transform;
    }

	public int getRounded() {
		if (imageOptions != null) {
			if (imageOptions.rounded != 0)
				return imageOptions.rounded;
			else 
				return 0;
		}
		return 0;
	}
    
    public Resizer getTargetImageSize() {
		if (hasSize()) {
    	    return getSize();
         }
       
	    Resizer resizer = imageWrapper.getTargetSize();
	    Resizer targetSize =  getScreenTargetSizeIfNesessary(resizer);
	
	    return getTargetSize(targetSize);
	}

	public Resizer getTargetSize(Resizer targetSize) {
		int maxWidthMemory = getMaxWidth();
		int maxHeightMemory = getMaxHeight();

		if (maxWidthMemory != 0) {
			if (targetSize.width > maxWidthMemory)
				targetSize.width = maxWidthMemory;
		}
		if (maxHeightMemory != 0) {
			if (targetSize.height > maxHeightMemory)
				targetSize.height = maxHeightMemory;
		}
		return targetSize;
	}
	
	public Resizer getSize() {
		return size;
	}
	
	public boolean hasSize() {
		if (size != null)
			if (size.width == 0 && size.height == 0)
				return false;
			else 
				return true;
		return false;
	}

	public int getMaxWidth() {
		int maxwidth = 0;
		if (size != null) {
			maxwidth = size.maxWidth;
		}
		if (maxwidth != 0)
			return maxwidth;
			
		if (options.getDiscMemoryCacheOptions() != null) {
			maxwidth =  options.getDiscMemoryCacheOptions().maxWidthForMemory;
		}
		return maxwidth;
	}
	
	public int getMaxHeight() {
		int maxheight = 0;
		if (size != null) {
			maxheight = size.maxHeight;
		}
		if (maxheight != 0)
			return maxheight;

		if (options.getDiscMemoryCacheOptions() != null) {
			maxheight =  options.getDiscMemoryCacheOptions().maxWidthForMemory;
		}
		return maxheight;
	}
	
	public int getMaxWidth(int width, int maxwidth) {
		//width = getMaxWidth();
		if (width > maxwidth)
			width = maxwidth;
		return width;
	}

	public int getMaxHeight(int height, int maxheight) {
		//height =  getMaxHeight();
		if (height > maxheight)
			height = maxheight;
		return height;
	}
	
	public int getFileDecodeQuality() {
		return options.decodeFileQuality;
	}
	
	public UriRequest getUri() {
		return loaduri;
	}
	
	public Handler getHandler() {
		if (handler != null)
			return handler;
		return options.getDefaultHandler();
	}
	
	public boolean shouldUseDeviceSizeByDefault() {
		return options.shouldUseScreenSizeByDefault();
	}
	
	public View getProgressView() {
		return progressView;
	}
	
	public boolean hasProgressCallback() {
		if (getProgressCallback() != null)
			if (getProgressCallback().get() != null)
				return true;
		return false;
	}
	
	public boolean hasCallback() {
		if (getCallback() != null)
			if (getCallback().get() != null)
				return true;
		return false;
	}
	
	public boolean transformPlaceHolder() {
		return transformPlaceHolder;
	}
	
	public boolean transformFailedHolder() {
		return transformFailedHolder;
	}
	
	public boolean shouldMargeBitmap() {
		return getMargeBitmap() != null ? true : false;
	}
	
	public BitmapMarge getMargeBitmap() {
		return margeBitmap;
	}
	
	public boolean isUseFindMemCacheAsHolder() {
		return useFindMemCacheAsHolder;
	}
	
	public boolean shouldCahceInMemory() {
		return cacheInMemory;
	}
	
	public boolean shouldCahceOnDisc() {
		return cacheOnDisc;
	}
	
	public boolean shouldCahceThumbnailOnDisc() {
		return saveThumbnailOnDisc;
	}
	
	public boolean shouldResize() {
		return options.discMemoryCacheOptions.maxWidthForMemory != 0 || options.discMemoryCacheOptions.maxWidthForMemory != 0
		|| 	size != null || imageOptions.centerCrop == true || imageOptions.fit == true || imageOptions.rotationDegrees != 0;
	}
	
	public boolean shouldAllowProgressView() {
		return progressView == null ? false : true;
	}
	
	public boolean shouldShowHolderImage() {
		boolean isShowHolder = placeHolderDrawable != null || placeHolderRes != 0 || shouldUseHolderUrl();
		if (isShowHolder == false)
			return isUseFindMemCacheAsHolder();
		else 
		    return isShowHolder;
	}

	public boolean shouldShowErrorImage() {
		return errorDrawable != null || errorRes != 0;
	}
		
	public boolean shouldUseHolderUrl() {
		return placeHolderUrl != null && !TextUtils.isEmpty(placeHolderUrl);
	}
	
	public boolean shouldSetScale() {
		return getSize() == null && getScaleType() != null && 
	    		   getBitmapDisplayer() instanceof DefaultTransformDisplayer;
	}
	
	public WeakReference<LoaderCallback> getCallback() {
		return callback;
	}
	
	public WeakReference<ProgressCallback> getProgressCallback() {
		return progressCallback;
	}
	
	public Drawable getImageLoadingHolder(Resources res) {
 		return placeHolderRes != 0 ? res.getDrawable(placeHolderRes) : placeHolderDrawable;
 	}

 	public Drawable getImageOnFailed(Resources res) {
 		return errorRes != 0 ? res.getDrawable(errorRes) : errorDrawable;
 	}
 	
	public ScaleType getScaleType() {
		return scaleType;
	}
	
	public boolean shouldModified() {
		return options.discCache != null && options.discCache instanceof LruDiscCache && 
				options.getDiscCacheDuration() == 0;
	}
	
	public ImageWrapper getImageWrapper() {
		return imageWrapper;
	}
	
    public ImageOptions getImageOptions() {
		return imageOptions;
	}

    public TransformDisplayer getBitmapDisplayer() {
		return transformDisplayer;
	}
    
    public AnimationStyle getAnimation() {
		if(imageOptions.fadein != 0 && imageOptions.animation == null) {
		   imageOptions.animation = new AnimationStyle(imageOptions.fadein);
		}
		return imageOptions.animation;
	}
    
    public boolean shouldRotateDegrees() {
    	return imageOptions.rotationDegrees != 0;
    }
    
    public boolean shouldRotatePivot() {
    	return imageOptions.rotationPivotX != 0 || imageOptions.rotationPivotY != 0;
    }
    
	public String getUriName() {
		return loaduri.getUriName();
	}
	
	private Resizer getScreenTargetSizeIfNesessary(Resizer resizer) {
		if (shouldUseDeviceSizeByDefault() == false)
			return resizer;
		
		DisplayMetrics dMetrics = options.getResources().getDisplayMetrics();
		if (resizer.width == 0) {
			resizer.width = dMetrics.widthPixels;
			//Toast.makeText(options.getContext(),"width "+dMetrics.widthPixels,Toast.LENGTH_SHORT).show();
		}
		if (resizer.height == 0) {
			resizer.height = dMetrics.heightPixels;
			//Toast.makeText(options.getContext(),"height "+dMetrics.heightPixels,Toast.LENGTH_SHORT).show();
		}
		return resizer;
	}
}
