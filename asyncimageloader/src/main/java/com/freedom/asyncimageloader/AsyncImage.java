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

import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ExecutorService;

import com.freedom.asyncimageloader.interfaces.DataEmiter;
import com.freedom.asyncimageloader.assist.BitmapDecoder;
import com.freedom.asyncimageloader.assist.Decoder;
import com.freedom.asyncimageloader.assist.GetBitmap;
import com.freedom.asyncimageloader.assist.Resizer;
import com.freedom.asyncimageloader.transform.Transform;
import com.freedom.asyncimageloader.displayer.TransformDisplayer;
import com.freedom.asyncimageloader.cache.DiscCache;
import com.freedom.asyncimageloader.cache.MemoryCache;
import com.freedom.asyncimageloader.callback.LoaderStatusCallback;
import com.freedom.asyncimageloader.download.Downloader;
import com.freedom.asyncimageloader.exception.MissingOptionsException;
import com.freedom.asyncimageloader.imagewrapper.ImageViewWrapper;
import com.freedom.asyncimageloader.imagewrapper.ImageWrapper;
import com.freedom.asyncimageloader.imagewrapper.MultiUriImageWrapper;
import com.freedom.asyncimageloader.imagewrapper.MultiViewWrapper;
import com.freedom.asyncimageloader.imagewrapper.SimpleViewWrapper;
import com.freedom.asyncimageloader.imagewrapper.ViewTargetWrapper;
import com.freedom.asyncimageloader.imagewrapper.ViewWrapperBackground;
import com.freedom.asyncimageloader.interfaces.Failed;
import com.freedom.asyncimageloader.interfaces.HttpSocket;
import com.freedom.asyncimageloader.interfaces.SocketResponse;
import com.freedom.asyncimageloader.interfaces.Failed.FailMethod;
import com.freedom.asyncimageloader.listener.ImageLoaderStatusListener;
import com.freedom.asyncimageloader.listener.LoaderCallback;
import com.freedom.asyncimageloader.memory.utils.MemoryCacheListener;
import com.freedom.asyncimageloader.network.NetworkManager;
import com.freedom.asyncimageloader.network.NetworkService;
import com.freedom.asyncimageloader.displayer.DefaultTransformDisplayer;
import com.freedom.asyncimageloader.uri.URLEncoded;
import com.freedom.asyncimageloader.uri.UriRequest;
import com.freedom.asyncimageloader.utils.BitmapUtils;
import com.freedom.asyncimageloader.utils.FileKeyGenerator;
import com.freedom.asyncimageloader.utils.Logg;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import java.util.*;
import android.widget.*;

import com.freedom.asyncimageloader.displayer.*;
import com.freedom.asyncimageloader.interfaces.*;
import com.freedom.asyncimageloader.callback.*;
import com.freedom.asyncimageloader.assist.*;

public class AsyncImage extends FImgConstants {

	static AsyncImage loaderStart = null;

	private Context context = null;
	private LoaderManager manager = null;
	LoaderOptions options = null;
	LoaderOptions defaultOptions = null;

	private boolean loggingEnabled = false;
	private boolean enableAsyncTaskDownloader = false;
	String defineActivityName;

	private static HashMap<String,AsyncImage> instances = new HashMap <String,AsyncImage>();

	LoaderStatusCallback statusCallback;

	private boolean localDownloading = false;
	boolean start = false;
	public static String FREEDOM_MULTI_URI = "freedom-multi-uri";
	public static String FREEDOM_MARGE_BITMAPS_URI = "freedom-marge-bitmaps-uri:";

	private AsyncImage(Context context) {
		this.context = context;
		init(options,context);
	}

	private AsyncImage() {}

	/** Returns loaderStart class ImageLoad */
	public static AsyncImage with(Context context) {
		if (context == null)
			Logg.warning("Can not pass null context in to instance loaderStart");
		if (loaderStart == null) {
			synchronized (AsyncImage.class) {
				if (loaderStart == null) {
					loaderStart = new AsyncImage(context);
				}
			}
		}
		return loaderStart;
	}

	/** Returns loaderStart class ImageLoad */
	public static AsyncImage getInstance() {
		if (loaderStart == null) {
			synchronized (AsyncImage.class) {
				if (loaderStart == null) {
					loaderStart = new AsyncImage();
				}
			}
		}
		return loaderStart;
	}

	public static AsyncImage getInstance(Context context) {
		return getInstance(context,context.toString());
	}

	public static AsyncImage getInstance(Context context,String name) {
		if (context == null)
			throw new NullPointerException( "Can not pass null context in to retrieve ion instance" );
		loaderStart = instances.get(name);
		if (loaderStart == null) {
			instances.put(name, loaderStart = new AsyncImage(context));
		}
		return loaderStart;
	}

	private AsyncImage(LoaderOptions configoptions) {
		init(configoptions,configoptions.context);
	}

	AsyncImage with(LoaderOptions configoptions) {
		synchronized (AsyncImage.class) {
			return new AsyncImage(configoptions);
		}
	}

	/**
	 * Initialize Image Loader options and Loader manager.
	 */
	public AsyncImage init(LoaderOptions configoptions,Context contextc) {
		if(options == null) {
			if(configoptions == null) {
				this.context = contextc;
				initDefaultOptions(contextc);
				initLoaderManager();
		    } else {
				this.options = configoptions;
				this.context = configoptions.context;
				initLoaderManager();
		    }
		}
		return this;
	}

	private void initLoaderManager() {
		localDownloading = false;
		loggingEnabled = options.loggingEnabled;
		enableAsyncTaskDownloader = options.AsyncTaskLoader;
        if (manager == null) {
    		manager = new LoaderManager(options);
	    }
        if (statusCallback == null) {
            statusCallback = new LoaderStatusCallback();
 	    }
	}

	public void initDefaultOptions(Context context) {
		if (context != null && options == null) {
			this.options = new LoaderOptions.OptionsBuilder(context)
				.setNetworkManager(new NetworkService(context))
				.setimageDecoder(new BitmapDecoder())
				.resetBeforeLoading()
				.discFileNameGenerator(new FileKeyGenerator.HASH())
				.fixSomeError()
				.removeBackground(true)
				//.checkValidUrlBeforeLoading(true)
				//.waitForUrlBeforeLoading(true)
				//.makeLoaderFastOnCreate()
				//.setMemoryCache(new LimitedMemoryCache(1024 * 1024 * 20))
				//.setDefaultScaleType(CENTER_CROP)
				//.decodeFileQuality(Utils.DEFAULT_DECODE_FILE_QUALITY)
				//.decodeFileQuality(120)
				//.setDownloader(true)
				//.defineEachActivity()
				//.enableSmoothLoading()
				//.setDiskCache(new NonDiscCache())
				//.setDiskCache(new SizeDiscCache(context))
				//.setMemoryCache(new NonMemoryCache())
				//.setMemoryCache(new LimitedMemoryCache(LimitedMemoryCache.TEN_MB_MEMORY_CACHE_SIZE))
				//.setMemoryCache(new TimeMemoryCache(TimeUtils.MINUTE.ONE_MINUTE))
				//.setMemoryCache(new LruCache(context,178))
				//.setMemoryCache(new SizeLruCache(context,23))
				//.setDiscTimeUtils(TimeUtils.MINUTE.TWO_MINUTE)
				.build();
		} else {
			//context null do nothing
		}
		checkForActive();
	}

	private void checkForActive() {
		if(options == null) {
			loaderStart = null;
		}
	}

	public void setLoaderOptions(LoaderOptions options) {
		this.options = options;
	}

	public LoaderRequest getRequestToLoad() {
		return new LoaderRequest(options,null);
	}

	public LoaderRequest imageUri(Object image,String name) {
		return new LoaderRequest(options,new UriRequest(image,name));
	}

	public LoaderRequest imageUri(Uri uri) {
		return new LoaderRequest(options,new UriRequest(uri,0));
	}

    /**
	 * Set image path with drawable file path or URLEncoded
	 * @param uri remote URLEncoded of the image to be download
	 * 
	 * start Sets Image URLEncoded
	 */
	public LoaderRequest imageUri(String uri) {
		/**
		 * The image URLEncoded.
		 * <p>
		 * This is a loading URLEncoded {@link #URI}.
		 */
		return imageUri(Uri.parse(uri != null ? uri : ""));
	}

	/**
	 * Set the image source file.
	 *
	 * @param uri the image file
	 */
	public LoaderRequest imageUri(File uri) {
		/**
		 * The File path.
		 * <p>
		 * This is a loading URLEncoded {@link #File path}.
		 */
		return imageUri(Uri.fromFile(uri != null ? uri : new File("")));
	}

	/**
	 * Set the image source file.
	 *
	 * @param drawable the image file
	 */
	public LoaderRequest imageUri(Drawable drawable,String name) {
		/**
		 * The Drawable image.
		 * <p>
		 * This is a loading Drawable {@link #Drawable}.
		 */
		return new LoaderRequest(options,new UriRequest(drawable,name));
	}

	/**
	 * Set the image in resource id.
	 *
	 * @param drawable the res id
	 */
	public LoaderRequest imageUri(int drawable) {

		/**
		 * The drawable Id.
		 * <p>
		 * This is a loading URLEncoded {@link #resourceId}.
		 */
		return new LoaderRequest(options,new UriRequest("",drawable));
    }

	public AsyncImage displayImage(LoaderSettings setting){
		loadImage(setting);
		return this;
	}

	public Bitmap directLoad(String uri) {
		GetBitmap Getbitmap = directLoad(uri,null,true);
		return Getbitmap == null ? null : Getbitmap.bitmap;
	}

	public Bitmap directLoad(String uri,Resizer size) {
		GetBitmap Getbitmap = directLoad(uri,size,true);
		return Getbitmap == null ? null : Getbitmap.bitmap;
	}

	/**
	 * Load and return loaded bitmap. getBitmap().bitmap.<br />
	 *  
	 * @param uri  loading URLEncoded.
	 * @param size Target size for loading image. 
	 * @param cacheToMemory check if should cache image to memory. 
	 * 
	 * @return {@link com.freedom.asyncimageloader.assist.GetBitmap GetBitmap}
	 */
	public GetBitmap directLoad(final String uri,final Resizer size,final boolean cacheToMemory) {
		checkOptions();
		LoaderRequest loader = new LoaderRequest(options,new UriRequest(Uri.parse(uri), 0));
		loader.cacheInMemory(cacheToMemory);
		if(size != null) {
			loader.resize(size,false);
		}
		return new GetBitmap(loader.asBitmap(),null);
	}


    /**
	 * Load and display image task to taskHunter.<br />
	 * uri    Image URLEncoded (i.e. "http://site.com/imgname.jpg", "R.drawable.image", "new File(filepath)")
	 *  
	 * when image start loading process.  callback.onStartLoad(null, new Exception(STARTED));
	 *      when loading process complete Listener.onSuccess(null, new Exception(LOADED));
	 *                        Load and display image rotate bitmap image to your own specific rotation
	 *                  can also scale image to any format example fit() or centerCrop()
	 *
	 * @throws IllegalArgumentException if {@link com.freedom.asyncimageloader.imagewrapper.ImageWrapper ImageWrapper} is null
	 */
	private void loadImage(LoaderSettings setting) {
		ImageWrapper imageWrapper = setting.getImageWrapper();
		String uriName = setting.getUriName();
		DataEmiter dataEmiter = new DataEmiter();
		LoaderCallback cb = setting.hasCallback() ? setting.getCallback().get() : null;
		ProgressCallback pcb = setting.hasProgressCallback() ? setting.getProgressCallback().get() : null;
		
		if (imageWrapper == null) {
			if(cb != null)
				cb.onFailed(new Failed(FailMethod.UNKNOWN_ERROR,"image wrapper null"),uriName,null);
			return;
		}

		if (uriName == null) {
			if(cb != null)
				cb.onFailed(new Failed(FailMethod.UNKNOWN_ERROR,"uri not set"),uriName,imageWrapper.getView());
			return;
		}

		if (TextUtils.isEmpty(uriName)) {
			if(cb != null)
				cb.onFailed(new Failed(FailMethod.UNKNOWN_ERROR,"uri not set"),uriName,imageWrapper.getView());
			return;
		}

		if (uriName.endsWith(AsyncImage.FREEDOM_MULTI_URI)) {
			return;
		}

		if (manager.isUriChange().containsKey(uriName)) {
			uriName =  manager.isUriChange().get(uriName);
		}

		final Resizer targetSize = setting.getTargetImageSize();// get image target size
		String memoryCacheKey = FileKeyGenerator.generateMemoryCacheKey(uriName, targetSize, setting, options.shouldAllowMultiplekeyCache());
		manager.addRequestView(imageWrapper,memoryCacheKey);
		dataEmiter.uri = uriName;

		final long startTime = SystemClock.uptimeMillis();
		dataEmiter.startTime = startTime; 

		if (setting.getScaleType() != null) {
			imageWrapper.setScaleType(setting.getScaleType());
		}

		Bitmap bitmap = options.getMemoryCache().get(memoryCacheKey);
		if (bitmap == null && setting.getSize() != null) {
			
			if (setting.getSize().useSimilarSizeIfFound) {
				final String incomingCacheKey = memoryCacheKey;
				Bitmap cacheBitmap = null;
				boolean isCacheBitmapFound = false;
				
				String isSimilarCache = manager.getCacheKeyForSimilarSize(incomingCacheKey);
				if (isSimilarCache != null) {
					bitmap = options.getMemoryCache().get(isSimilarCache);
				}
				
				if (bitmap == null) {
					for (String cacheKey : options.getMemoryCache().retrieveListOfCacheKey()) {
						if (!cacheKey.contains(FREEDOM_MARGE_BITMAPS_URI)) {
							String normalSaveCacheKey = getFirstWord(cacheKey,FileKeyGenerator.FIND_CACHE_SIZE_KEY);
							String incomingSaveCacheKey = getFirstWord(incomingCacheKey,FileKeyGenerator.FIND_CACHE_SIZE_KEY);
							boolean isFound = normalSaveCacheKey.equals(incomingSaveCacheKey);

							if (isFound) {
								cacheBitmap = options.getMemoryCache().get(cacheKey);
								if (cacheBitmap.getHeight() > targetSize.height) {
									manager.putCacheKeyForSimilarSize(incomingCacheKey, cacheKey);
									isCacheBitmapFound = true;
									break;
								} 			
							}
						}
					}
				}

				if (isCacheBitmapFound) {
					bitmap = cacheBitmap;
					Toast.makeText(options.context, bitmap != null ? "is found in cache with similar size" :"uri find in cache but not loaded ",Toast.LENGTH_LONG).show();
				}
			}
		}
		if (bitmap != null && !setting.skipMemoryCache) {
			dataEmiter.infoValue = "loaded from memory cache start time: "+startTime;
			dataEmiter.loadedFrom = PhotoLoadFrom.CACHE;
			dataEmiter.bitmap = bitmap;
			setting.getBitmapDisplayer().onLoaded(setting,dataEmiter,imageWrapper,setting.imageOptions.rounded);

			if(cb != null) {
				cb.onSuccess(dataEmiter,imageWrapper.getView());
			}
			if(pcb != null) {
				pcb.onProgressUpdate(100, 100);
			}
		} else {
			boolean isCacheHolderFound = false;
			manager.checkAndShowProgressView(setting.progressView,true);    
			if (setting.shouldShowHolderImage()) {

				if (setting.isUseFindMemCacheAsHolder()) {
					String incomingCacheKey = memoryCacheKey;
					
					for (String cacheKey : options.getMemoryCache().retrieveListOfCacheKey()) {
						if (!cacheKey.contains(FREEDOM_MARGE_BITMAPS_URI)) {
							String normalSaveCacheKey = getFirstWord(cacheKey,FileKeyGenerator.FIND_CACHE_KEY);
							String incomingSaveCacheKey = getFirstWord(incomingCacheKey,FileKeyGenerator.FIND_CACHE_KEY);
							boolean isFound = normalSaveCacheKey.equals(incomingSaveCacheKey);

							if (isFound) {
								isCacheHolderFound = true;
								dataEmiter.bitmap = options.getMemoryCache().get(cacheKey);
								setting.getBitmapDisplayer().onLoaded(setting,dataEmiter,imageWrapper,setting.getRounded());
								break ;
							}
						}
					}
				} 
				if (!isCacheHolderFound) {
					Drawable drawable = setting.getImageLoadingHolder(options.getResources());
					if (drawable != null)
						setting.getBitmapDisplayer().placeHolder(setting,imageWrapper, drawable);
				}
			} else {
				if(setting.resetView) {
					imageWrapper.onImageBitmap(null);
				}
			}
			RequestData data = new RequestData(new UriRequest(uriName,setting.loaduri.image,
															  setting.getUri().uridrawable) ,imageWrapper,
											   targetSize,dataEmiter,setting, memoryCacheKey,manager.putLockUri(uriName));

			if (enableAsyncTaskDownloader || setting.useAsyncTask) {
				AsyncTaskLoader asynctask = new AsyncTaskLoader(uriName, manager, data, cb, pcb, options, imageWrapper, localDownloading);
				manager.submitTask(asynctask);
			} else {
				SimpleTaskLoader photoTask = new SimpleTaskLoader(manager, data, cb, pcb, options);
				manager.submitTask(photoTask);
			} 
		}
	}

	private String getFirstWord(String uri,String isIndex) {
		if (isIndex != null && isIndex.length() == 0)
			return "";

		if (isIndex != null && isIndex.length() != 0) {
			int i = uri.indexOf(isIndex);
			if (i > 0) {
				return uri.substring(0, i);
			}
		}
		return uri;
	}

	private String getLastWord(String uri,String isIndex) {
		if (isIndex != null && isIndex.length() == 0) 
			return uri;

		if (isIndex != null && isIndex.length() != 0) {
			int i = uri.lastIndexOf(isIndex);
			if (i > 0) {
				return uri.substring(i, uri.length());
			}
		}
		return uri;
	}

	/**
	 * This call disc cache from internal memory
	 */
	public DiscCache getDiscCache() {
		checkOptions();
		return options.discCache;
	}

	ImageLoaderMonitor monitor = null;
	public ImageLoaderMonitor getMonitor() {
		if (monitor == null) {
			checkOptions();
			monitor = new ImageLoaderMonitor(options, manager);
		}
		return monitor;
	}

	public LoaderOptions getOptions() {
		return options;
	}

	/**
	 * This call memory cache from internal memory
	 */
	public MemoryCache getMemoryCache() {
		checkOptions();
		return options.memoryCache;
	}

	/**
	 * This Clears all the images from disc .
	 * Clears disc Cache.
	 *
	 * @throws IllegalStateException if disc return error
	 */
	public void clearDiscCache() {
		try {
			checkOptions();
			options.discCache.clear();
		} catch (Exception e ) {
			if (loggingEnabled) log_e(e,"Disc cache error.");
	        throw new IllegalStateException("Disc cache error.");
		}
	}

	/**
	 * This Clears all bitmap from memory cache .
	 * @throws Exception 
	 *
	 * @throws IllegalStateException if memory return error
	 */
	public void clearMemoryCache() {
		try {
			checkOptions();
			if(options.shouldDefineEachActivity()) {
				options.memoryCache.clear();
			} else {
				options.memoryCache.clear();
			}
	    } catch (Exception e ) {
	    	log_e(e,"Memory cache reset error.");
	        throw new IllegalStateException("Memory cache error.");
		}
	}

	/**
	 * This Clears the bitmap memory cache and discCache images completely.
	 * @throws Exception 
	 *
	 * @throws IllegalStateException if memory and disc return error
	 */
	public void clearCaches() {
		try {
			options.memoryCache.clear();
			options.discCache.clear();
		} catch (Exception e ) {
			log_e(e,"Memory and disc cache reset error.");
			throw new IllegalStateException("Memory and disc cache error.");
		}
    }

	/**
	 * current Loading view
	 * URLEncoded View for Current Loading task Returns URLEncoded of the image which is loading at the current moment
	 * {@link android.widget.ImageView imageView}
	 */
	public String getRequest(ImageView imageView) {
		return manager.requestUriView(new ImageViewWrapper(imageView, null));
	}

	/**
	 * cancel current Loading Cancel URLEncoded of image which is Loading Task
	 * {@link android.widget.ImageView imageView}
	 */
	public void cancelRequest(ImageView imageView) {
		cancelRequest(new ImageViewWrapper(imageView, null));
	}

	/**
	 * cancel current Loading Cancel URLEncoded of image which is Loading Task
	 * {@link android.widget.ImageView imageView}
	 */
	public void cancelRequest(ImageViewWrapper wrapper) {
		manager.cancelRequestView(wrapper);
	}

	public void cancelRequest(LoaderSettings oldRequest) {
		if (oldRequest != null) {
			manager.cancelRequestView(oldRequest.getImageWrapper());
		}
	}

	/**
	 * cancel Lock UrI
	 * manager.removeBlockUri(Uri) Cancel Lock URLEncoded
	 */
	public void removeLockUri(Uri uri) {
		if(manager != null) 
			manager.removeBlockUri(uri.toString());
	}

	/**
	 * clear all Lock UrI
	 * manager.clearBlockUri() Clear lock URLEncoded
	 */
	public void clearLockUri() {
		if(manager != null) 
			manager.clearBlockUri();
	}

	/**
	 * clear all Loading Task
	 * manager.clearViewKeyCache() Clear all loading URLEncoded of ImageView
	 * {@link android.widget.ImageView imageView}
	 */
	public void clearLoadingRequest() {
		if(manager != null) 
			manager.clearViewKeyCache();
	}

	/**
	 * check LoaderOptions options is init()
	 */
	private void checkOptions() {
		if(options == null) {
			try {
				checkOptionsInit();
			} catch (MissingOptionsException e) {
				log_d(e.getMessage());
			}
   		}
	}

	private void checkOptionsInit() throws MissingOptionsException {
		if(options == null) {
			initDefaultOptions(context);
			throw new MissingOptionsException("freedom loader options not init");
   		}
	}

	/**
	 * images will not be download form Internet.<br />.
	 * following options will not be considered <ul> <li>{#setDownloader(downloader)}</li>
	 */
	public void denyNetworkDownloading() {
		denyNetworkDownloading(true);
	}

	/**
	 * if true images will not be download form Internet.<br />. 
 	 * <b>NOTE:</b> If you set not to use network for loading images following options will not be considered
 	 * <ul>
 	 * <li>{#setDownloader(downloader)}</li>
	 */
	public void denyNetworkDownloading(boolean neverUseNetwork) {
		manager.neverUseNetwork(neverUseNetwork);
	}

	/**
	 * images will not be loaded from disc and local loading will not be use.
	 */
	public void allowOnlyNetworkDownloading() {
		allowOnlyNetworkDownloading(true);
	}

	/**
	 * images will not be loaded from disc and local loading will not be use.<br />.
	 * <b>NOTE:</b> If you set to use only network for loading images memory cache will be consider
	 */
	public void allowOnlyNetworkDownloading(boolean useOnlyNetwork) {
		manager.useOnlyNetwork(useOnlyNetwork);
	}

	/**
	 * Use AsyncTask to load images.
	 */
	public void useAsyncTaskLoader() {
		useAsyncTaskLoader(true,false);
	}

	/**
	 * Use AsyncTask to load images.
	 */
	public void useLocalAsyncTaskLoader() {
		useAsyncTaskLoader(true,false);
	}

	/**
	 * Use AsyncTask to load images.
	 */
	public void useAsyncTaskLoader(boolean enableAsyncTaskLoader,boolean localLoading) {
		LoaderOptions newOptions = new LoaderOptions.OptionsBuilder(context)
			.renewWithOld(options)
			.enableAsyncTaskLoader(enableAsyncTaskLoader)
			.build();
		this.options = newOptions;
		this.localDownloading = localLoading;
		this.enableAsyncTaskDownloader = enableAsyncTaskLoader;
	}

	public boolean putCacheKeyForFailedUrl(String url,boolean isFailed) {
		return manager.putCacheKeyForFailedUrl(url,isFailed);
	}

	public boolean removeCacheKeyForFailedUrl(String url) {
		return manager.removeCacheKeyForFailedUrl(url);
	}

	/**
	 * register Call back.
	 */
	public void registerLoaderCallback(ImageLoaderStatusListener statuscallback) {
		statusCallback.registerLoaderListener(statuscallback);
	}

	/**
	 * unregister Call back.
	 */
	public void unRegisterLoaderCallback(ImageLoaderStatusListener statuscallback) {
		statusCallback.unRegisterLoaderListener(statuscallback);
	}

	/**
	 * register cache Call back.
	 */
	public void registerCacheCallback(MemoryCacheListener cacheListener) {
		options.memoryCache.init("addCacheCallback", cacheListener);
	}

	public String getMargeCacheWithKey(String uri, String key) {
		StringBuffer builder = new StringBuffer();
		builder.append(key);
		builder.append('\n');
		builder.append('\n');
		List<String> cacheRequest = getMemoryCache().retrieveListOfCacheKey();

		for (String cacheKey : cacheRequest) {

			if (cacheKey.contains(AsyncImage.FREEDOM_MARGE_BITMAPS_URI)) {
				if (cacheKey.startsWith(uri) && cacheKey.endsWith(builder.toString())) {
					return cacheKey;
				} else {
					//DialogUtils.showShortToast(act, "is not find");
				}
			}
		}
		return null;
	}

	/**
	 * unregister cache Call back.
	 */
	public void unRegisterCacheCallback(MemoryCacheListener cacheListener) {
		options.memoryCache.init("removeCallback", cacheListener);
	}

	/**
	 * Resume tasks resume all pause task {resume} display images.<br />.
	 */
	public boolean resume() {
		if(loggingEnabled) log_d(LOADER_RESUME);
		statusCallback.onResume(this);
		return manager.resume();
	}

	public void forceResume() {
    	for (int i = 1; i <= 3;) {
			resume(); return;
    	}
    }

	/**
	 * Start tasks start at fresh start to loading new tasks.<br />.
	 */
	public void start() {
		if(loggingEnabled) log_d(LOADER_START);
		manager.start();
	}

	public void start(boolean startOnComing) {
		if(startOnComing == false) {
			this.start = true;
			statusCallback.onStart(this);
		}
	}

	/**
	 * Shut down all the running and incoming loading image tasks.<br />
	 * to start loader again call method {start()}.
	 */
	public void shutdown() {
		if(loggingEnabled) log_d(LOADER_SHUTDOWN);
		manager.shutdown();
		statusCallback.onShutdown(this);
	}

	/**
	 * Check if loading executors is Shut down .<br />
	 * Returns <tt>true</tt> - if loading Executors has shutdown
	 */
	public boolean isShutdown() {
		return manager != null && manager.isShutdown() && 
			(manager.isLoadingExecutorShutdown() || manager.isCacheExecutorShutdown());
	}

	/**
	 * Check if task has been Pause  .<br />
	 * Returns <tt>true</tt> - if task Pause
	 */
	public boolean isPause() {
		return manager != null && manager.isPause();
	}

	/**
	 * Force Pause all the loading tasks. and remove all bitmap inside memory cache.<br />
	 * to resume all pause tasks call {resume()}.
	 */
	public void forcePause() {
		forcePause(0);
	}

	/**
	 * Force Pause all the loading tasks. and remove all bitmap inside memory cache.<br />
	 * and resume after specific delay time {@code mills} complete.
	 */
	public void forcePause(int time) {
		pause(time);
		clearMemoryCache();
	}

	/**
	 * Pause all the current loading tasks. tasks has been pause.<br />
	 * and resume after specific time {@code mills}.
	 */
	public void pause(int time) {
		if(loggingEnabled) log_d(LOADER_PAUSE);
		if(manager != null) manager.pause(time);
	}

	/**
	 * Pause all the current loading tasks. tasks has been pause.<br />
	 * to resume all pause tasks call {resume()}.
	 */
	public void pause() {
		if(loggingEnabled) log_d(LOADER_PAUSE);
		if(manager != null) manager.pause();
		statusCallback.onPause(this);
	}

	/**
	 * Destroy ImageLoader and kill all loading tasks and all incoming tasks.
	 * to start ImageLoader again call method { start()}.
	 */
	public void destroy() {
		if(loggingEnabled) log_d(LOADER_DESTORY);
		shutdown();
		stopLoader();
		statusCallback.onDestory(this);
	}

	/**
	 * Stop loading animation from imageWrapper if required
	 */
	public void stopAnimation(ImageWrapper imageWrapper) {
		manager.stopAnimation(imageWrapper);
	}

	/**
	 * Stop loader to null
	 */
	public void stopLoader() {
		loaderStart = null;
		options = null;
		manager = null;
	}

	/**
	 * Set to define activities
	 */
	public void defineActivitys() {
		options = new LoaderOptions.OptionsBuilder(context)
			.renewWithOld(options)
			.defineEachActivity()
			.build();
	}

	@Override
	public String toString() {
		String status = isShutdown() ? "ImageLoader is shutdown" : isPause() ? "ImageLoader is pause" 
			: "ImageLoader is running";
		StringWriter stringAppend = new StringWriter()
			.append(" status: ",status)
			.append(" memory cache: ",options.memoryCache.toString())
			.flush();
		return stringAppend.toString();
	}

	public class LoaderRequest {
		/** A Place Holder Image from drawable to be used while the image is loading.  */
		int placeHolderRes = 0; 
		Drawable placeHolderDrawable = null;
		String placeHolderUrl;
		/** A Error Image from drawable to be used while the image load failed. If requested. */
		Drawable errorDrawable = null;
		int errorRes = 0; 

		/** progress view for image loading process. */
		View progressView = null;

        boolean saveThumbnailOnDisc = false;
		/** boolean reset Image drawable. */
		boolean resetView = false;
		boolean useFindMemCacheAsHolder = false;
		/** boolean true cache in memory. */
		boolean cacheInMemory = true;
		boolean skipMemoryCache = false;
		boolean transformHolder = false;
		boolean cacheOnDisc = true;
		boolean skipDiscCache = false;
		boolean useAsyncTask = false;
		int skipImageLoading = 0;

	 	Bitmap.Config bitmapConfig = null;
	 	Options decodingOptions = null;
		/** ImageSize resize bitmap Image. */
		Resizer size = null;

		/** ImageView where bitmap image are display. */
		ImageWrapper imageWrapper = null;
		ImageOptions imageOptions = new ImageOptions();
		BitmapMarge marge = null;

		boolean defineActivity = true;
		String defineActivityName = "name";

		/** Image Uri Object contain disk cache or network. */
		UriRequest loaduri  = null;
	    /** imageView scale type. */
		ScaleType scaleType = null; 

		/** Bitmap Rounded is where the image is rounded with animation. */
		TransformDisplayer transformDisplayer = new DefaultTransformDisplayer();
		Transform transform = null;

		/** Options init discCache and memoryCache and DownLoader and listener. */
		public LoaderOptions options = null;
		Handler handler = null;
		WeakReference<LoaderCallback> loadingCallback = null;
		WeakReference<ProgressCallback> progressCallback = null;

		LoaderRequest(LoaderOptions options,UriRequest loaduri) {
			this.options = options;
			this.loaduri = loaduri;
		}

		/** Sets Scale Type */
		public LoaderRequest setScaleType(ScaleType scaleType) {
			this.scaleType = scaleType;
			return this;
		}

		public LoaderRequest append(UriRequest loaduri) {
			this.loaduri = loaduri;
			return this;
		}

		/**
		 * Sets if should skip loading image from disc cache or should not.
		 */
		public LoaderRequest skipDiscCache() {
			this.skipDiscCache = true;
			return this;
		}

		/**
		 * Sets if should skip loading image from memory cache or should not.
		 */
		public LoaderRequest skipMemoryCache() {
			this.skipMemoryCache = true;
			return this;
		}

		/**
		 * Sets if loaded image should be cached in memory or should not.
		 *
		 * @param cacheInMemory if should cache in memory or not
		 */
		public LoaderRequest cacheInMemory(boolean cacheInMemory) {
			return cacheInMemory(cacheInMemory,false);
		}

		/**
		 * Sets if loaded image should be cached in memory or should not.
		 *
		 * @param cacheInMemory if should cache in memory or not
		 * @param skipMemoryCache if should skip memory cache or not
		 */
		public LoaderRequest cacheInMemory(boolean cacheInMemory,boolean skipMemoryCache) {
			this.cacheInMemory = cacheInMemory;
			this.skipMemoryCache = skipMemoryCache;
			return this;
		}

		/**
		 * Sets if loaded image should be cached on disc or should not.
		 *
		 * @param cacheOnDisc if should cache on disc or not
		 */
		public LoaderRequest cacheOnDisc(boolean cacheOnDisc) {
			return cacheOnDisc(cacheOnDisc,false);
		}

		/**
		 * Sets if loaded image should be cached on disc or should not.
		 *
		 * @param cacheOnDisc if should cache on disc or not
		 * @param skipDiscCache if should skip disc cache or not
		 */
		public LoaderRequest cacheOnDisc(boolean cacheOnDisc,boolean skipDiscCache) {
			this.cacheOnDisc = cacheOnDisc;
			this.skipDiscCache = skipDiscCache;
			return this;
		}

		/** Define Activity with activity name and set defineActivity to true */
		public LoaderRequest defineActivity(String name) {
			this.defineActivity = true;
			this.defineActivityName = name;
			return this;
		}

		public LoaderRequest margeBitmapWith(Bitmap bitmap) {
			this.marge = new BitmapMarge(bitmap);
			this.marge.setKeyUri(loaduri.uri.toString());
			return this;
		}

		public LoaderRequest margeBitmapWith(List<Bitmap> bitmaps) {
			this.marge = new BitmapMarge(bitmaps);
			this.marge.setKeyUri(loaduri.uri.toString());
			return this;
		}

		public LoaderRequest margeBitmapWith(BitmapMarge marge) {
			this.marge = marge;
			this.marge.setKeyUri(loaduri.uri.toString());
			return this;
		}

		/**
		 * Check if url can be find in cache with any value
		 * if it can be find then set it as place holder
		 */
		public LoaderRequest useFindMemCacheAsHolder() {
			return useFindMemCacheAsHolder(0);
		}
		
		/**
		 * Check if url can be find in cache with any value
		 * @param isUse if true can find cache or if false then do nothing 
		 * if it can be find then set it as place holder
		 */
		public LoaderRequest useFindMemCacheAsHolder(boolean isUse) {
			useFindMemCacheAsHolder = isUse;
			return this;
		}

		/**
		 * Check if url can be find in cache with any value
		 * if it can be find then set it as place holder
		 * @param holder holder to use for place holder if mem cache uri not found
		 */
		public LoaderRequest useFindMemCacheAsHolder(int holder) {
			useFindMemCacheAsHolder = true;
			placeHolderRes = holder;
			return this;
		}

		/**
		 * Check if url can be find in cache with any value
		 * if it can be find then set it as place holder
		 * @param holder holder to use for place holder if mem cache uri not found
		 */
		public LoaderRequest useFindMemCacheAsHolder(Drawable holder) {
			useFindMemCacheAsHolder = true;
			placeHolderDrawable = holder;
			return this;
		}

		/**
		 * A holder from drawable to be used during the image loading process. if the image is
		 * not find in the memory cache then this resource will be set on the imageWrapper
		 * {@link com.freedom.asyncimageloader.imagewrapper.ImageWrapper ImageWrapper}.
		 * <p>
		 * you can reset place holder by pass in {@code null}.
		 */
		public LoaderRequest placeHolder(Drawable drawable) {
			placeHolderDrawable = drawable;
			return this;
		}

		/**
		 * A holder from drawable to be used during the image loading process. if the image is
		 * not find in the memory cache then this resource will be set on the imageWrapper
		 * {@link com.freedom.asyncimageloader.imagewrapper.ImageWrapper ImageWrapper}.
		 */
		public LoaderRequest placeHolder(int placeHolder) {
			this.placeHolderRes = placeHolder;
			return this;
		}

		/**
		 * Image will be displayed in {@link com.freedom.asyncimageloader.imagewrapper.ImageWrapper ImageWrapper}
		 * if some error occurs during loading images or decoding or download 
		 * you can reset error image by pass in {@code null}.
		 */
		public LoaderRequest failed(Drawable drawable) {
			errorDrawable = drawable;
			return this;
		}

		/**
		 * Image will be displayed in {@link com.freedom.asyncimageloader.imagewrapper.ImageWrapper ImageWrapper}
		 * if some error occurs during loading images or decoding or download 
		 *
		 * @param Uri Image resource
		 */
		public LoaderRequest failed(int Uri) {
			this.errorRes = Uri;
			return this;
		}

		/**
		 * Set the target width and height for image.
		 *
		 * @param width the target width
		 * @param height the target height
		 */
		public LoaderRequest resize(int width, int height) {
			return resize(width,height,false);
		}
		

		/**
		 * Resize with cache bitmap
		 *
		 * @useSmimilarSizeIfFound check if bitmap is found 
		        with the same uri with high size
		 */
		public LoaderRequest resize(boolean useSimilarSizeIfFound) {
			return resize(new Resizer(useSimilarSizeIfFound));
		}

		public LoaderRequest resize(ImageView imageView) {
			if (imageView == null)
				return this;

			int width = imageView.getMeasuredWidth();
			int height = imageView.getMeasuredHeight();

			if (width != 0 || height != 0)
				this.size = new Resizer(width,height);
			return this;
		}

		/**
		 * Set the target width and height for image.
		 *
		 * @param width the target width
		 * @param height the target height
		 * @param saveThumbnailOnDisc cache thumbnail on disc with width and height
		 */
		public LoaderRequest resize(int width, int height,boolean saveThumbnailOnDisc) {
			return resize(new Resizer(width,height),saveThumbnailOnDisc);
		}

		/**
		 * Set the target width and height for image.
		 *
		 * @param resizer needed data for cache size
		 * @param saveThumbnailOnDisc cache thumbnail on disc with resize target
		 */
		public LoaderRequest resize(Resizer resizer,boolean saveThumbnailOnDisc) {
			this.saveThumbnailOnDisc = saveThumbnailOnDisc;
			return resize(resizer);
		}
		
		/**
		 * Set the target width and height for image.
		 *
		 * @param resizer needed data for cache size
	    */
		public LoaderRequest resize(Resizer resizer) {
			if (resizer == null)
				return this;
		    if ((resizer.getWidth() != 0 && resizer.getHeight() != 0)
			           || resizer.useSimilarSizeIfFound) 
			    this.size = resizer;

			return this;
		}
		
		public LoaderRequest grayScale() {
			this.imageOptions.grayScale = true;
			return this;
		}

		public LoaderRequest fade() {
			return fade(1000);
		}
		
		public LoaderRequest fade(int fade) {
			this.imageOptions.fadein = fade;
			return this;
		}
		
		/**
		 * Set auto rotate to respect image Exif orientation.
		 *
		 * @param rotationDegrees rotate
		 */
		public LoaderRequest rotation(int rotationDegrees) {
			this.imageOptions.rotationDegrees = rotationDegrees;
			return this;
		}

		/**
		 * Set skip Loading.
		 *
		 * @param skipImageLoading
		 */
		public LoaderRequest skipImageLoading(int skipImageLoading) {
			this.skipImageLoading = skipImageLoading;
			return this;
		}

		/**
		 * Enable transform holder.
		 */
		public LoaderRequest transformHolder() {
			this.transformHolder = true;
			return this;
		}

		/**
		 * Set the round corner radius.
		 */
		public LoaderRequest rounded() {
			transformDisplayer(new RoundedDisplayer());
			return rounded(0);
		}

		/**
		 * Set the round corner radius.
		 *
		 * @param cornerRadius
		 */
		public LoaderRequest rounded(int cornerRadius) {
			this.imageOptions.rounded = cornerRadius;
			return this;
		}

		/**
		 * Enabled AsyncTask
		 *
		 * @param enabledAsyncTask
		 */
		public LoaderRequest enabledAsyncTask(boolean enabledAsyncTask) {
			this.useAsyncTask = enabledAsyncTask;
			return this;
		}

		/**
		 * center Crops an image inside with request required by {@link #resize(int, int)}
	     * scales the image so that it fills the requested bounds.
		 */
		public LoaderRequest centerCrop() {
			this.imageOptions.centerCrop = true;
			return this;
		}

		/**
		 * Attempt to resize the image to fit exactly imageView the view Size {@link ImageView}
		 * it will scale the {@link android.widget.ImageView ImageView} and fit the screen.
		 */
		public LoaderRequest fit() {
			this.imageOptions.fit = true;
			return this;
		}

		public LoaderRequest unfit() {
			this.imageOptions.fit = false;
			return this;
		}

		public LoaderRequest centerInside() {
			this.imageOptions.centerInside = true;
			return this;
		}

		/**
		 * Sets Decoder which will be use for decoding image stream.<br />
		 * Default - {@link com.freedom.asyncimageloader.assist.BitmapDecoder BitmapDecoder}
		 */
		public LoaderRequest decoder(Decoder decoder) {
			this.options = AsyncImage.this.options = new LoaderOptions.OptionsBuilder(context)
				.renewWithOld(options)
				.setimageDecoder(decoder)
				.build();
			return this;
		}

		/**
		 * show the image loading progress View {@link android.view.View View}
		 * Pass in progress bar view, or any other view for loading progress and will be 
		 * visible {@link android.view.View} or invisible {@link android.view.View}.
		 */
		public LoaderRequest progress(View progressView) {
			this.progressView = progressView;
			return this;
		}

		/** Set if the image View Drawable should remove  */
		public LoaderRequest resetViewDrawable() {
			this.resetView = true;
			return this;
		}

		/**
		 * Sets your own custom {@code handler} for displaying images
		 */
		public LoaderRequest handler(Handler handler) {
			this.handler = handler;
			return this;
		}

		/** 
		 * Sets Transform Displayer
		 *  Default {com.freedom.imageunited.bitmap.DefaultTransformDisplayer DefaultTransformDisplayer}.
		 */
		public LoaderRequest transformDisplayer(TransformDisplayer transformDisplayer) {
			this.transformDisplayer = transformDisplayer;
			return this;
		}

		/** 
		 * Sets transformBitmap . 
		 */
		public LoaderRequest transform(Transform transformBitmap) {
			this.transform = transformBitmap;
			return this;
		}

		public LoaderRequest imageOptions(ImageOptions imageOptions) {
			if (imageOptions == null) {
				log_d("Image Options should not be null");
				return this;
			}
			this.imageOptions = imageOptions;
			return this;
		}

		/**
		 * Sets to decode the image with your Specify BitmapConfig.
		 * <p>
		 * Add a custom config to be applied to the bitmap image.
		 */
		public LoaderRequest bitmapConfig(Bitmap.Config config) {
		    this.bitmapConfig = config;
		    return this;
		}

		/**
		 * Sets image options for decoding.<br />
		 * <b>NOTE:</b> This option {@link #bitmapConfig(android.graphics.Bitmap.Config) bitmapConfig()} wont be consider.
		 */
		public LoaderRequest decodingOptions(Options decodingOptions) {
			this.decodingOptions = decodingOptions;
			return this;
		}

		/**
		 * SET Progress Listener when image loading process is loading bytes.
		 */
		public LoaderRequest callback(ProgressCallback progressCallback) {
			this.progressCallback = new WeakReference<ProgressCallback>(progressCallback);
			return this;
		}

		public LoaderRequest callback(LoaderCallback callback) {
			this.loadingCallback = new WeakReference<LoaderCallback>(callback);
			return this;
		}

		public void loadinto(Hashtable<View,String> mPendingMultiLoad) {
			this.imageWrapper = new MultiUriImageWrapper(mPendingMultiLoad,this);
			
			return ; //doLoaders();
		}

		/**
		 * SET three views to be waited for loading image to be loaded
		 * 
		 * @param view1 view1 waiting for image to be loaded
		 * @param view2 view2 waiting for image to be loaded
		 * @param view2 view3 waiting for image to be loaded
		 */
		public void loadinto(View view1,View view2,View view3) {
			this.imageWrapper = new MultiViewWrapper(getUri(loaduri),view1,view2,view3);
			doLoaders();
		}

		/**
		 * SET List of View to be waited for loading image to be loaded
		 * 
		 * @param listImageViews list of view waiting for image to be loaded
		 */
		public void loadinto(List<Reference<View>> listImageViews) {
			this.imageWrapper = new MultiViewWrapper(getUri(loaduri),listImageViews);
			doLoaders();
		}

		/**
		 * public class ImageViewHolder extends SimpleViewWrapper {
		 * ImageView imageView;
		 *
		 *   public ImageViewHolder(ImageView view) {
		 *	    imageView = view;
		 *   }
		 *   
		 *   {@literal @}Override public void onSuccess(GetBitmap bitmap,
		 *				ResponseData data, ImageView view) {
		 *     imageView.setImageBitmap(bitmap));
		 *   }
		 *
		 *   {@literal @}Override public void onFailed(Failed failed,
		 *              ImageView view) {
		 *     imageView.setBackgroundResource(R.drawable.failed_image);
		 *   }
		 *
		 *   {@literal @}Override public void onStartLoad(ResponseData data,
		 *				ImageView view, Drawable holderDrawable) {
		 *     imageView.setImageDrawable(holderDrawable);
		 *   }
		 * }
		 * image will be loaded to callback
	     */
		public void loadinto(SimpleViewWrapper callback) {
			this.imageWrapper = callback;
			this.loadingCallback =  new WeakReference<LoaderCallback>(callback);
			this.imageWrapper = imageWrapper.putUri(getUri(loaduri));
			doLoaders();
		}

		public void loadinto(LoaderCallback callback) {
			this.imageWrapper = new SimpleViewWrapper();
			this.loadingCallback =  new WeakReference<LoaderCallback>(callback);
			this.imageWrapper = imageWrapper.putUri(getUri(loaduri));
			doLoaders();
		}

		/**
		 * @param imageView Into ImageView where the image will show when it load complete
		 * load from memory cache or discCache or drawable or assets or network
		 *      into(android.widget.ImageView, com.android.freedom.listener.LoaderCallback).
		 * SET Listener when image started complete error
		 *  @param callback {@linkplain LoaderCallback Listener}
		 *  
		 *  when image start loading process.
		 *        onStartLoad(ResponseData, android.widget.ImageView, Drawable);
		 *       
		 *  when loading process complete 
		 *        onSuccess(GetBitmap, ResponseData, android.widget.ImageView);
		 *        
		 *  when loading process failed  
		 *        onFailed(Failed);
		 */
		public void loadinto(ImageView imageView,LoaderCallback callback) {
			if (callback != null) {
				this.loadingCallback =  new WeakReference<LoaderCallback>(callback);
			}
			loadinto(imageView);
		}

		public void loadinto(View view,LoaderCallback callback) {
			if (callback != null)
				this.loadingCallback =  new WeakReference<LoaderCallback>(callback);
			loadinto(view);
		}

		/**
		 * SET Into ImageView where the image will be show when it load complete 
		 * load from memory cache or disc cache or network or others
		 * 
		 * @param imageView
		 */
		public void loadinto(ImageView imageView) {
			this.imageWrapper = new ImageViewWrapper(imageView,getUri(loaduri));
			if (imageOptions.fit) {
			    if (size == null) {
					int width = imageView.getMeasuredWidth();
					int height = imageView.getMeasuredHeight();
					if (width == 0 || height == 0) {
						transformDisplayer.placeHolder(new LoaderSettings(this),imageWrapper, placeHolderRes != 0 ? 
													   options.getResources().getDrawable(placeHolderRes) : placeHolderDrawable);

						manager.putTargetRequest(imageView, new ViewTargetWrapper(this, 
																				  loadingCallback != null ? loadingCallback.get() : null, imageView));	    	
						return;
					}
					size = new Resizer(width, height);
				}
			}
			doLoaders();
		    manager.cancelRequest(imageView);
		}

		/**
		 * SET Into View where the image will be show when it load complete 
		 * load from memory cache or disc cache or network or others
		 * 
		 * @param view
		 */
		public void loadinto(View view) {
			if (view instanceof ImageView) {
				loadinto((ImageView) view);
			} else {
				this.imageWrapper = new ViewWrapperBackground(getUri(loaduri),view);
				if (imageOptions.fit) {
					if (size == null) {
						int width = view.getMeasuredWidth();
						int height = view.getMeasuredHeight();
						if (width == 0 || height == 0) {
							transformDisplayer.placeHolder(new LoaderSettings(this),imageWrapper, placeHolderRes != 0 ? 
														   options.getResources().getDrawable(placeHolderRes) : placeHolderDrawable);

							manager.putTargetRequest(view, new ViewTargetWrapper(this, 
																				 loadingCallback != null ? loadingCallback.get() : null, view));
							return;
						}
						size = new Resizer(width, height);
					}
				}
				doLoaders();
			}
		}

		public void fetch() {
			if(imageWrapper == null) {
				this.imageWrapper = new SimpleViewWrapper();
			}
			if(transformDisplayer == null) {
				this.transformDisplayer = new DefaultTransformDisplayer();
			}
			doLoaders();
		}

		public String asString() {
			return BitmapUtils.BitMapToString(asBitmap());
		}

		public Bitmap asBitmap() {
			this.imageWrapper  = new SimpleViewWrapper().putUri(loaduri.getUriName());
		    LoaderSettings setting = new LoaderSettings(this);
		    String uriName = setting.getUriName();
			Bitmap bitmap = null;
			DataEmiter dataEmiter= new DataEmiter();

		    if(manager.isUriChange().containsKey(uriName)) {
				uriName =  manager.isUriChange().get(uriName);
			}
			final Resizer targetSize = setting.getTargetImageSize();

		    String memoryCacheKey = FileKeyGenerator.generateMemoryCacheKey(uriName, targetSize, setting, options. shouldAllowMultiplekeyCache());
		    manager.addRequestView(imageWrapper,memoryCacheKey);

			final long startTime = SystemClock.uptimeMillis();
			dataEmiter.startTime = startTime; 

		    if (setting.getSize() == null && setting.getScaleType() != null) {
				imageWrapper.setScaleType(setting.getScaleType());
		    }

			bitmap = options.memoryCache.get(memoryCacheKey);

		    if (bitmap == null) {
		        BitmapLoader loader = new BitmapLoader(this.options,manager,new RequestData(new UriRequest(uriName,
																										   setting.getUri().uridrawable),imageWrapper,targetSize,dataEmiter, setting,memoryCacheKey,  
																							manager.putLockUri(uriName)));

			    bitmap = loader.load(uriName);
		    }
			bitmap = setting.getBitmapDisplayer().transformBitmap(setting.getSize(),bitmap,setting);
			return bitmap;
		}

		/** Create the immutable */
		public LoaderSettings buildRequest() {
			fetch();
			doLoaders(false);
			return new LoaderSettings(this);
		}

		public void doLoaders() {
			doLoaders(true);
		}
		/** Create the immutable  */
		public void doLoaders(boolean isLoad) {
			if(this.options != null) {
				if(scaleType == null) {
					if(shouldUseDefaultScale() && this.options.shouldUseScaleType()) 
						scaleType = this.options.defaultScaleType;
				}
			}

			if (options.isQueryEnabled() && loaduri != null) {
				URLEncoded mencode = URLEncoded.parse(loaduri.uri);
				if (mencode.hasQuery() && mencode.contains("type") && mencode.get("type").equals("freedom")) {

					if (size == null && mencode.contains("w") && mencode.contains("h")) {
						size = new Resizer(mencode.getInt("w"),mencode.getInt("h"));
					}
					if (mencode.contains("rotation")) {
						imageOptions.rotationDegrees = mencode.getInt("rotation");
					} 
					if (mencode.contains("rounded")) {
						int rounded = mencode.getInt("rounded");
						if (rounded !=0)
							imageOptions.rounded = rounded;
						else
							transformDisplayer(new RoundedDisplayer());
			        }
                    if (mencode.contains("fade")) {
						imageOptions.fadein = mencode.getInt("fade");
 			        }
				    if (mencode.contains("grayScale")) {
						imageOptions.grayScale = mencode.getBoolean("grayScale");
				    } 
                    if (mencode.contains("time")) {
						DiscMemoryCacheOptions optio = new  DiscMemoryCacheOptions();
						optio.memoryCacheDuration(mencode.getInt("time"));

						AsyncImage.this.options = new LoaderOptions.OptionsBuilder(context)
							.renewWithOld(AsyncImage.this.options)
							.discMemoryCacheOptions(optio).build();
  			        }
				    if (mencode.contains("transformHolder")) {
						transformHolder = mencode.getBoolean("transformHolder");
				    } 
				    if (mencode.contains("useFindMemCacheAsHolder")) {
						useFindMemCacheAsHolder = mencode.getBoolean("useFindMemCacheAsHolder");
			        } 
                    if (mencode.contains("centerCrop")) {
						imageOptions.centerCrop = mencode.getBoolean("centerCrop");
 		    	    } 
				    else if (mencode.contains("centerInside")) {
						imageOptions.centerInside = mencode.getBoolean("centerInside");
 		    	    } 
				    else if (mencode.contains("fit")) {
						imageOptions.fit = mencode.getBoolean("fit");
					} 
					loaduri.uri = Uri.parse(mencode.removeQuery());
				} 
			} else {
		    	//Remove query if contains type and equals ("freedom")
			}
			if (isLoad)
				displayImage(new LoaderSettings(this));
		}

		private boolean shouldUseDefaultScale() {
			return size == null || !imageOptions.centerCrop|| !imageOptions.fit;
		}

		private String getUri(UriRequest loaduri) {
			return loaduri.uri != null ? loaduri.uri.toString() : String.valueOf(loaduri.getUriDrawable());
		}

	}

	public static class Builder {
		private LoaderOptions.OptionsBuilder config;

		/** Create Options from old to a new instance */
		public Builder(Context context) {
			LoaderOptions options = new AsyncImage().getOptions();
			this.config = new LoaderOptions.OptionsBuilder(context)
				.renewWithOld(options != null ? options : new LoaderOptions.OptionsBuilder(context).build());
		}

		/** Create Options from old to a new instance. */
		public Builder(LoaderOptions options) {
			this.config = new LoaderOptions.OptionsBuilder(options.context).renewWithOld(options);
		}

		/**  AsyncTask enables proper and easy use of the UI thread.*/
		public Builder enableAsyncTaskDownloader(boolean enableAsyncTaskLoader) {
			config.enableAsyncTaskLoader(enableAsyncTaskLoader);
			return this;
		}

		/** Sets ImageDownloader which will be use to download images.<br />*/
		public Builder downloader(Downloader downloader) {
			if (downloader == null) {
				throw new IllegalArgumentException("Downloader can npt be null.");
			}
			config.setDownloader(downloader);
			return this;
		}

		/** Specify the executor service for loading images in the background. */
		public Builder executor(ExecutorService executorService) {
			if (executorService == null) {
				throw new IllegalArgumentException("Executor service must not be null.");
			}
			config.setExecutor(executorService);
			return this;
		}

		/** Sets memory cache for {@link android.graphics.Bitmap bitmaps}.<br />*/
		public Builder memoryCache(MemoryCache memoryCache) {
			if (memoryCache == null) {
				throw new IllegalArgumentException("Memory cache can not be null.");
			}
			config.setMemoryCache(memoryCache);
			return this;
		}

		/** Sets disc cache for storing download images.<br />*/
		public Builder discCache(DiscCache discCache) {
			if (discCache == null) {
				throw new IllegalArgumentException("Listener must not be null.");
			}
			config.setDiskCache(discCache);
			return this;
		}

		/** Enabled Query. */
		public Builder enabledQuery() {
			config.enabledQuery();
			return this;
		}

		/** Sets Network Manager */
		public Builder networManager(NetworkManager networkManager) {
			if (networkManager == null) {
				throw new IllegalArgumentException("Transformer must not be null.");
			}
			config.setNetworkManager(networkManager);
			return this;
		}

		/** Sets SocketConnection for downloading image from network*/
		public Builder socketHttp(HttpSocket httpSocket) {
			config.socketHttp(httpSocket);
			return this;
		}

		/**  Sets whether debug logging is enabled. */
		public Builder loggingEnabled(boolean enabled) {
			config.enabledLogging(enabled);
			return this;
		}

		/** Create the {@link AsyncImage} instance. */
		public AsyncImage build() {
			LoaderOptions options = config.build();
			return new AsyncImage().with(options);
		}
	}

	private void log_e(Throwable e,String message) {
		if (loggingEnabled) Logg.error(e,message);
	}

	private void log_d(String message) {
		if (loggingEnabled) Logg.debug(message);
	}
}
