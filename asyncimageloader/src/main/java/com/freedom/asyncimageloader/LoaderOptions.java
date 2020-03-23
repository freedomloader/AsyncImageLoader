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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import com.freedom.asyncimageloader.assist.Decoder;
import com.freedom.asyncimageloader.assist.ImageStreamLoader;
import com.freedom.asyncimageloader.cache.DiscCache;
import com.freedom.asyncimageloader.cache.MemoryCache;
import com.freedom.asyncimageloader.disc.DefaultDiscWriter;
import com.freedom.asyncimageloader.disc.DiscWriter;
import com.freedom.asyncimageloader.disc.NonDiscCache;
import com.freedom.asyncimageloader.download.Downloader;
import com.freedom.asyncimageloader.interfaces.HttpSocket;
import com.freedom.asyncimageloader.interfaces.HttpSocket.SocketConnection;
import com.freedom.asyncimageloader.memory.NonMemoryCache;
import com.freedom.asyncimageloader.memory.utils.MemoryCacheListener;
import com.freedom.asyncimageloader.memory.utils.MemoryCacheOpen;
import com.freedom.asyncimageloader.network.NetworkManager;
import com.freedom.asyncimageloader.uri.UriUtil;
import com.freedom.asyncimageloader.utils.FileNameGenerator;
import com.freedom.asyncimageloader.utils.Logg;
import com.freedom.asyncimageloader.utils.Utility;

import android.content.Context;
import android.content.res.Resources;
import android.widget.ImageView.ScaleType;
import java.util.*;
import com.freedom.asyncimageloader.download.*;
import android.graphics.*;
import android.os.*;

public final class LoaderOptions {

	final Downloader downloader;
	final DiscCache discCache;
	final MemoryCache memoryCache;
	final MemoryCache defaultMemoryCache;
	final MemoryCacheListener memoryCacheListener;
	final Context context;
	final NetworkManager networkManager;
	final ImageStreamLoader imageStream;
	
	final Handler defaultHandler;
	final Decoder imageDecoder;
	final int decodeFileQuality;
	final long discCacheDuration;
	final Executor cacheTaskExecutor;
	final Executor fatchExecutor;
	final boolean allowMultiplekeyCache;
	final boolean makeLoaderFastOnCreate;
	final boolean useDeviceScreenSize;
	final long deleteDiscCacheFileOlder;
	final FileNameGenerator discFileNameGenerator;
	final DiscWriter discWriter;
	final UriControlBuilder uriControl;
	final DiscMemoryCacheOptions discMemoryCacheOptions;
	final HttpSocket httpSocket;
	/** true logging Enabled. */
	final boolean loggingEnabled;
	/** true if true image we be download using AsyncTask. */
	final boolean AsyncTaskLoader;
	final File discDirectory;
	final boolean cacheDiscBitmapTransform;
	final boolean cacheBitmapTransform;
	final boolean removeBackground;
	final boolean defineEachActivity;
	final boolean resetBeforeLoading ;
	final boolean checkNetworkBeforeLoading;
	final boolean removeQueryFromUrl;
	final boolean enabledQuery ;
	final boolean enableSmoothLoading;
	final boolean multiCallbackEnabled;
	final boolean fixSomeError;
	final boolean initExecutor;
	final int threadPoolSize;
	final int threadPriority;
	final ScaleType defaultScaleType;
	final String discCahceFileNameExtension;
	final List<String> allowWebsites;
	final boolean isCheckValidUrlBeforeLoading;
	final boolean waitForUrlBeforeLoading;
	final ImageValidator imageValidator;

	private LoaderOptions(final OptionsBuilder options) {
		allowWebsites = options.allowWebsites;
		isCheckValidUrlBeforeLoading = options.isCheckValidUrlBeforeLoading;
		waitForUrlBeforeLoading = options.waitForUrlBeforeLoading;
		imageValidator = options.imageValidator;
		httpSocket = options.httpSocket;
		context = options.context;
		uriControl = options.uriControl;
		loggingEnabled = options.loggingEnabled;
		downloader = options.downloader;
		cacheBitmapTransform = options.cacheBitmapTransform;
		cacheDiscBitmapTransform = options.cacheDiscBitmapTransform;
		removeBackground = options.removeBackground;
		memoryCache = options.memoryCache;
		defaultMemoryCache = options.defaultMemoryCache;
		memoryCacheListener = options.memoryCacheListener;
		allowMultiplekeyCache = options.allowMultiplekeyCache;
		discCache = options.discCache;
		imageDecoder = options.imageDecoder;
		decodeFileQuality = options.decodeFileQuality;
		networkManager = options.networkManager;
		discDirectory = options.discDirectory;
		discCacheDuration = options.discCacheDuration;
		discMemoryCacheOptions = options.discMemoryCacheOptions;
		defineEachActivity = options.defineEachActivity;
		multiCallbackEnabled = options.multiCallbackEnabled;
		resetBeforeLoading = options.resetBeforeLoading;
		checkNetworkBeforeLoading = options.checkNetworkBeforeLoading;
		enableSmoothLoading = options.enableSmoothLoading;
		fixSomeError = options.fixSomeError;
		makeLoaderFastOnCreate = options.makeLoaderFastOnCreate;
		useDeviceScreenSize = options.useDeviceScreenSize;
		initExecutor = options.initExecutor;
		fatchExecutor = options.fatchExecutor;
		cacheTaskExecutor = options.cacheTaskExecutor;
		threadPoolSize = options.threadPoolSize;
		removeQueryFromUrl = options.removeQueryFromUrl;
		enabledQuery = options.enabledQuery;
		threadPriority = options.threadPriority;
		AsyncTaskLoader = options.AsyncTaskLoader;
		defaultScaleType = options.defaultScaleType;
		discCahceFileNameExtension = options.discCahceFileNameExtension;
		discFileNameGenerator = options.discFileNameGenerator;
		deleteDiscCacheFileOlder = options.deleteDiscCacheFileOlder;
		discWriter = options.discWriter;
		imageStream = options.imageStream;
		defaultHandler = options.defaultHandler;
	}

	public HttpSocket getHttpSocket() {
		return httpSocket;
	}
	
	public ImageStreamLoader getStreamLoader() {
		return imageStream;
	}
	
	public Decoder getImageDecoder() {
		return imageDecoder;
	}
	
	public Downloader getDownloader() {
		return downloader;
	}
	
	public DiscCache getDiscCache() {
		return discCache;
	}
	
	public FileNameGenerator getDiscFileNameGenerator() {
		return discFileNameGenerator;
	}
	
	public DiscMemoryCacheOptions getDiscMemoryCacheOptions() {
		return discMemoryCacheOptions;
	}
	
	public DiscWriter getDiscWriter() {
		return discWriter;
	}
	
	public MemoryCache getMemoryCache() {
		return memoryCache;
	}
	
	public MemoryCache getDefaultMemoryCache() {
		return defaultMemoryCache;
	}
	
	public MemoryCacheListener getMemoryCacheListener() {
		return memoryCacheListener;
	}
	
	public NetworkManager getNetworkManager() {
		return networkManager;
	}
	
	public ImageStreamLoader getImageStreamLoader() {
		return imageStream;
	}
	
	public Context getContext() {
		return context;
	}
	
	public Handler getDefaultHandler() {
		return defaultHandler;
	}
	
	public Resources getResources() {
		return getContext().getResources();
	}

	public boolean isLoggingEnabled() {
		return loggingEnabled;
	}

	public boolean shouldUseScreenSizeByDefault() {
		return useDeviceScreenSize;
	}
	
	public boolean shouldDefineEachActivity() {
		return defineEachActivity;
	}
	
	public boolean shouldResetBeforeLoading() {
		return resetBeforeLoading;
	}
	
	public boolean shouldAllowMultiplekeyCache() {
		return allowMultiplekeyCache;
	}
	
	public boolean shouldEnableSmoothLoading() {
		return enableSmoothLoading;
	}
	
	public boolean shouldFastOnCreate() {
		return makeLoaderFastOnCreate;
	}
	
	public boolean shouldFixSomeError() {
		return fixSomeError;
	}
	
	public List<String> getAllowWebsites() {
		return allowWebsites;
	}

	public boolean isWaitForUrlBeforeLoading() {
		return waitForUrlBeforeLoading;
	}
	
	public boolean isCheckValidUrlBeforeLoading() {
		return isCheckValidUrlBeforeLoading;
	}
	
	public ImageValidator getImageValidator() {
		return imageValidator;
	}
	
	public boolean shouldRemoveQueryFromUrl() {
		return removeQueryFromUrl;
	}
	
	public boolean isQueryEnabled() {
		return enabledQuery;
	}
	
	public boolean isCacheBitmapTransform() {
		return cacheBitmapTransform ? cacheBitmapTransform : cacheDiscBitmapTransform;
	}

	public boolean isMemoryCacheBitmapTransform() {
		return cacheBitmapTransform;
	}

	public boolean isDiscCacheBitmapTransform() {
		return cacheDiscBitmapTransform;
	}
	
	public boolean shouldRemoveBackground() {
		return removeBackground;
	}
	
	public boolean shouldCheckNetworkBeforeLoading() {
		return checkNetworkBeforeLoading;
	}
	
	public boolean shouldUseScaleType() {
		return defaultScaleType != null;
	}
	
	/**  Check if max size is set for disc cache*/
 	public boolean shouldCacheDiscMaxSizes() {
 		return discMemoryCacheOptions.maxWidthForDisc != 0 && discMemoryCacheOptions.maxHeightForDisc != 0;
 	}
 	
 	/**  Check if max size is set for disc cache*/
 	public boolean shouldCacheMemoryMaxSizes() {
 		return discMemoryCacheOptions.maxWidthForMemory != 0 && discMemoryCacheOptions.maxHeightForMemory != 0;
 	}

	public long getDiscCacheDuration() {
		return discCacheDuration;
	}

	public OptionsBuilder toOptionsBuilder() {
		return new LoaderOptions.OptionsBuilder(context)
				.renewWithOld(this);
	}

	public static class OptionsBuilder {
		private List<String> allowWebsites = null;
		private boolean isCheckValidUrlBeforeLoading = true;
		private boolean waitForUrlBeforeLoading = false;
		private ImageValidator imageValidator = null;
		private Context context;
		private UriControlBuilder uriControl = null;
		private DiscMemoryCacheOptions discMemoryCacheOptions = null;
		private HttpSocket httpSocket = null;
		/** true if true image we be download using AsyncTask. */
		private boolean AsyncTaskLoader = false;
		/** true logging Enabled. */
		private boolean loggingEnabled = false;
		private Handler defaultHandler = Utility.createHandler();
		private Downloader downloader;
		private MemoryCache memoryCache;
		private MemoryCache defaultMemoryCache;
		private MemoryCacheListener memoryCacheListener;
		private DiscCache discCache;
		private String discCahceFileNameExtension = "";
		private FileNameGenerator discFileNameGenerator;
		private DiscWriter discWriter;
		private ScaleType defaultScaleType = null;
		private NetworkManager networkManager = null;
		private ImageStreamLoader imageStream = null;
		private boolean cacheBitmapTransform = false;
		private boolean cacheDiscBitmapTransform = false;
		private boolean removeBackground = false;
		private boolean allowMultiplekeyCache = true;
		private boolean removeQueryFromUrl = false;
		private boolean enabledQuery = false;
		private boolean defineEachActivity = false;
		private boolean resetBeforeLoading = false;
		private boolean checkNetworkBeforeLoading = false;
		private boolean multiCallbackEnabled = false;
		private boolean enableSmoothLoading  = false;
		private boolean fixSomeError  = false;
		private boolean makeLoaderFastOnCreate  = false;
		private boolean useDeviceScreenSize = false;
		private boolean initExecutor = false;
		private Executor cacheTaskExecutor = null;
		private Executor fatchExecutor = null;
		private ExecutorOptions executorOptions = null;
		private Decoder imageDecoder = null;
		private int decodeFileQuality = 0;
		private File discDirectory = null;
		private long discCacheDuration  = 0;
		private long deleteDiscCacheFileOlder = 0;
		private int threadPoolSize = ExecutorOptions.DEFAULT_THREAD_POOL_SIZE;
		private int threadPriority = ExecutorOptions.DEFAULT_THREAD_PRIORITY;
		/** Sets Builder Context */

		public OptionsBuilder(Context context) {
			this.context = context.getApplicationContext();
		}
		
		public OptionsBuilder setDefaultHandler(Handler handler) {
			this.defaultHandler = handler;
			return this;
		}

		/** 
		 * Sets URLEncoded Control Builder
		 *  
		 * @param uriControlBuilder
         */
		public OptionsBuilder uriControl(UriControlBuilder uriControlBuilder) {
			this.uriControl = uriControlBuilder;
			return this;
		}
		
		/** 
		 * Sets Cache options
		 *  
		 * @param discMemoryCacheOptions
         */
		public OptionsBuilder discMemoryCacheOptions(DiscMemoryCacheOptions discMemoryCacheOptions) {
			this.discMemoryCacheOptions = discMemoryCacheOptions;
			return this;
		}

		/** 
		 * Sets SocketConnection for downloading image from network
		 *  
		 * @param httpSocket
         */
		public OptionsBuilder socketHttp(HttpSocket httpSocket) {
			this.httpSocket = httpSocket;
			return this;
		}

		/** 
		 * Sets SocketConnection for downloading image from network
		 *  
		 * @param httpConnection
         */
		public OptionsBuilder socketHttp(SocketConnection httpConnection) {
			this.httpSocket = httpConnection.build();
			return this;
		}

		/** 
		 *  Sets Scale Type
		 *  Default - {@link android.widget.ImageView.ScaleType}
		 *  Default Scale the image uniformly (maintain the image's aspect ratio) so that both dimensions (width and height) 
		 *  of the image will be equal to or larger than the corresponding dimension of the view (minus padding).
         */
		public OptionsBuilder setDefaultScaleType(ScaleType defaultScaleType) {
			if (defaultScaleType == null) {
		        throw new IllegalArgumentException("default scaleType should not be null.");
		    }
			if (this.defaultScaleType != null) {
		    	Logg.warning("default scaleType already set.");
		    }
			this.defaultScaleType = defaultScaleType;
			return this;
		}
		
		/**
		 * AsyncTask enables proper and easy use of the UI thread. This class
		 * allows to perform background operations and publish results on the UI
		 * thread without having to manipulate threads and/or handlers.
		 */
		public OptionsBuilder enableAsyncTaskLoader(boolean enableAsyncTaskLoader) {
		    if (this.AsyncTaskLoader == true) {
		    	Logg.warning("Async Task Downloader already set.");
		    }
			this.AsyncTaskLoader = enableAsyncTaskLoader;
			return this;
		}

		/**
		 * Sets ImageDownloader which will be use to download images.<br />
		 * Default - {@link com.freedom.asyncimageloader.download.ImageDownloader ImageDownloader}
		 */
		public OptionsBuilder setDownloader(Downloader downloader) {
			if (downloader == null) {
		        throw new IllegalArgumentException("Downloader should not be null.");
		    }
		    if (this.downloader != null) {
		    	Logg.warning("Downloader already set.");
		    }
			this.downloader = downloader;
			return this;
		}
		
		public OptionsBuilder setAllowWebsites(List<String> websites) {
			allowWebsites = websites;
			return this;
		}
		
		public OptionsBuilder waitForUrlBeforeLoading(boolean isWait) {
			waitForUrlBeforeLoading = isWait;
			return this;
		}
		
		public OptionsBuilder checkValidUrlBeforeLoading(boolean isCheck) {
			isCheckValidUrlBeforeLoading = isCheck;
			return this;
		}
		
		public OptionsBuilder setImageValidator(ImageValidator validator) {
			imageValidator = validator;
			return this;
		}
		
		/** Sets discCache Directory */
		public OptionsBuilder setDiscCacheDirectory(File directory) {
			this.discDirectory = directory;
			return this;
		}
		
		public OptionsBuilder shouldUseDeviceScreenSize() {
			this.useDeviceScreenSize = true;
			return this;
		}
		
		public OptionsBuilder enableMultiCallback() {
			this.multiCallbackEnabled = true;
			return this;
		}

		/**
         * Prevent Loading task from using transform displayer all the time
		 using this method allow the task to be store inside memory cache with cache transform only once
		 if the task has been store it will store with the transform displayer
         * @return self
         */
		public OptionsBuilder cacheBitmapTransformDisplayer() {
			cacheBitmapTransform = true;
			return this;
		}

		/**
         * Prevent Loading task from using transform displayer all the time
		 using this method allow the task to be store inside disc. cache with cache transform only once
		 if the task has been store it will store with the transform displayer
         * @return self
         */
		public OptionsBuilder cacheDiscBitmapTransformDisplayer() {
			cacheDiscBitmapTransform = true;
			return this;
		}

		/** 
		 * @param duration The time, in milliseconds, that
		 * image should be clears if mills is older 
		 */
		public OptionsBuilder setDiscCacheDuration(long duration) {
			this.discCacheDuration = duration;
			return this;
		}
		
		/** delete disc cache file older than specified time */
		public OptionsBuilder deleteDiscCacheFileOlderThan(long deleteDiscCacheFileOlder) {
			this.deleteDiscCacheFileOlder = deleteDiscCacheFileOlder;
			return this;
		}
		
		public OptionsBuilder discWriter(DiscWriter discWriter) {
			this.discWriter = discWriter;
			return this;
		}
		
		/**
		 * Sets disc cache for storing download images.<br />
		 * <br />
         * A LRU Cache allows you to save images in memory instead of going to a
         * server every time. This allows your APP to respond much quicker to changes
         * faster than going to the network for an image.
		 */
		public OptionsBuilder setDiskCache(DiscCache discCache) {
			if (discCache == null) {
		        throw new IllegalArgumentException("discCache should not be null.");
		    }
		    if (this.discCache != null) {
		    	Logg.warning("discCache already set.");
		    }
			this.discCache = discCache;
			return this;
		}
		
		/**
         * Prevent Loading task from using a disk cache when loading
         * @return self
         */
		 public OptionsBuilder disableDiskCache() {
	          return setDiskCache(new NonDiscCache());
	     }
		 
		 /**
	      * Prevent loading images before view created make sure view created before.
	      * task start loading images using this method improve performance
	      *
	      * @return self
	      */
		 public OptionsBuilder makeLoaderFastOnCreate() {
	          return makeLoaderFastOnCreate(true);
	     }
		 
		 /**
	      * Enabled or disabled view for loading images if true view should be created.
	      * before loading images else loading images onStart
	      *
	      * @return self
	      */
		 public OptionsBuilder makeLoaderFastOnCreate(boolean shouldFastOnCreate) {
			  makeLoaderFastOnCreate  = shouldFastOnCreate;
	          return this;
	     }
		 
		 /**
	      * Prevent Loading task from using a memory cache when loading.
	      *
	      * @return self
	      */
	      public OptionsBuilder disableMemoryCache() {
	          return setMemoryCache(new NonMemoryCache());
	      }

		/** 
		 * Remove any background or black background from image
		 * @shouldRemove tell if should remove or not
		 * image should be only on bitmap
		 */
		public OptionsBuilder removeBackground(boolean shouldRemove) {
			removeBackground = shouldRemove;
			return this;
		}
		
	     /**
		  * Remove Query from url.
		  *
	      * @return self
		  */
		 public OptionsBuilder removeQueryFromUrl() {
			 removeQueryFromUrl = true;
			 return this;
		 }
		      
		 /**
		  * Enabled Query.
		  *
		  * @return self
		  */
		 public OptionsBuilder enabledQuery() {
			 enabledQuery = true;
			 return this;
		 }
	      
		/**
		 * Sets memory cache for {@link android.graphics.Bitmap bitmaps}.<br />
		 * Default - {@link com.freedom.asyncimageloader.memory.LimitedMemoryCache SoftMemoryCache}
		 */
	    public OptionsBuilder setMemoryCache(MemoryCache memoryCache) {
	    	if (memoryCache == null) {
		        throw new IllegalArgumentException("Memory Cache should not be null.");
		    }
	        if (this.defaultMemoryCache != null) {
	        	Logg.warning("Memory cache already set.");
	        }
	        this.defaultMemoryCache = memoryCache;
	        return this;
	    }
		
		/**
		 * Sets memory cache call back.
		 */
	    public OptionsBuilder setMemoryCacheListener(MemoryCacheListener memoryCacheListener) {
	    	if (memoryCacheListener == null) {
		        throw new IllegalArgumentException("Memory Cache call back should not be null.");
		    }
	        if (this.memoryCacheListener != null) {
	        	Logg.warning("Memory cache call back already set.");
	        }
	        this.memoryCacheListener = memoryCacheListener;
	        return this;
	    }
	    
		/** Specify Executor */
		public OptionsBuilder setExecutor(Executor executor) {
			if (executor == null) {
		        throw new IllegalArgumentException("Executor should not be null.");
		    }
		    if (this.fatchExecutor != null) {
		    	Logg.warning("taskExecutor already set.");
		    }
			this.fatchExecutor = executor;
			return this;
		}
		
		/** 
		 * Sets Executor Options
		 *  
		 * @param executorOptions
         */
		public OptionsBuilder setExecutor(ExecutorOptions executorOptions) {
			if (executorOptions == null) {
		        throw new IllegalArgumentException("Executor Builder should not be null.");
		    }
		    if (this.fatchExecutor != null) {
		        Logg.warning("Executor already set.");
		    }
			this.executorOptions = executorOptions;
			return this;
		}

		/** Sets Executor Options */
		public OptionsBuilder setExecutor(int threadPoolSize,int threadPriority,long keepAlive,
				TimeUnit timeUnit,BlockingQueue<Runnable> taskQueue) {
		    if (this.fatchExecutor != null) {
		    	Logg.warning("taskExecutor already set.");
		    }
			this.executorOptions = new ExecutorOptions()
			.keepAlive(keepAlive)
			.threadPoolSize(threadPoolSize)
			.threadPriority(threadPriority)
			.timeUnit(timeUnit)
			.taskQueue(taskQueue)
			.build();
			return this;
		}
		
		/** Sets DiscCahce FileName Extension*/
		public OptionsBuilder discFileNameExtension(String discCahceFileNameExtension) {
			this.discCahceFileNameExtension = discCahceFileNameExtension;
			return this;
		}
		
		/** Sets DiscCahce FileName Extension*/
		public OptionsBuilder enabledDefaultDiscFileNameExtension() {
			this.discCahceFileNameExtension = UriUtil.FREEDOM_EXTENSION;
			return this;
		}
		
		/**
		 * Sets file name generator for cached images in disc.<br />
		 * Default - {@link com.freedom.asyncimageloader.utils.FileKeyGenerator.HASH FileKeyGenerator.HASH}.
		 */
		public OptionsBuilder discFileNameGenerator(FileNameGenerator discFileNameGenerator) {
			if (discFileNameGenerator == null) {
		        throw new IllegalArgumentException("File Name Generator should not be null.");
		    }
		    if (this.discFileNameGenerator != null) {
		        Logg.warning("Disc File Name Generator already set.");
		    }
			this.discFileNameGenerator = discFileNameGenerator;
			return this;
		}
		
		/**
		 * Sets the thread pool size for image loading tasks.<br />
		 * Default - {#DEFAULT_THREAD_POOL_SIZE ExecutorOptions.DEFAULT_THREAD_POOL_SIZE}
		 */
		public OptionsBuilder setThreadPoolSize(int threadPoolSize) {
			this.threadPoolSize = threadPoolSize;
			return this;
		}
		
		/**
		 * Sets the thread priority for loading threads.
		 * Default - {#DEFAULT_THREAD_PRIORITY ExecutorOptions.DEFAULT_THREAD_PRIORITY}
		 */
		public OptionsBuilder setThreadPriority(int threadPriority) {
			if (threadPriority < Thread.MIN_PRIORITY) {
				this.threadPriority = Thread.MIN_PRIORITY;
			} else {
				this.threadPriority = threadPriority;
			}
			return this;
		}
		
		/**
		 * Image with multiple size in memory cache 
		 * When you load image with size to memory cache and later you try to show
		 * this image in another size so the image will be decoded with new size to memory cache
		 * By default: multiple sizes is allow to be use in memory cache</b>. 
		 * if Set <tt>false</tt> cached image in memory with size will be remove before loading new request.
		 */
		public OptionsBuilder denyMultiplekeyCacheInMemory() {
			this.allowMultiplekeyCache = false;
			return this;
		}
		
		/** if <tt>true</tt> define Each Activity for image loader . */
		public OptionsBuilder defineEachActivity() {
			this.defineEachActivity = true;
			return this;
		}
		
		/** 
		 * if <tt>true</tt> then Reset Last Loading info Before Loading for image loader . 
		 * By default: last loading info will be reset.<br />
		 */
		public OptionsBuilder resetBeforeLoading() {
			this.resetBeforeLoading = true;
			return this;
		}
		
		/**
		 * Check if connection is available before loading images from Internet.<br />
		 * Default - false check connections is disallow
		 */
		public OptionsBuilder checkNetworkBeforeLoading() {
			this.checkNetworkBeforeLoading = true;
			return this;
		}
		
		/**
	     * Sets whether debug logging is enabled. {Freedom-Loader}
	     * <p>
	     * <b>WARNING:</b> Enabling this will result in detail logs
         * Set to <tt>false</tt> to prevent logs.
	     */
		public OptionsBuilder enabledLogging(boolean enabledLogging) {
			this.loggingEnabled = enabledLogging;
			return this;
		}
		
		/** Sets decode Quality to reduce memory cache */
		public OptionsBuilder decodeFileQuality(int decodeQuality) {
			this.decodeFileQuality = decodeQuality;
			return this;
		}
		
		/** Enable Smooth Loading */
		public OptionsBuilder enableSmoothLoading() {
			this.enableSmoothLoading = true;
			return this;
		}
		
		/**
		 * Fix errors if Set to <tt>true</tt> any error that occurred will be fix
		 * By Default - false fix errors is disallow
		 */
		public OptionsBuilder fixSomeError() {
			this.fixSomeError = true;
			return this;
		}
 
		/** 
		 * Sets Network Manager 
		 *  Default - {@link com.freedom.asyncimageloader.network.NetworkService NetworkService}
		 */
		public OptionsBuilder setNetworkManager(NetworkManager networkManager) {
			if (networkManager == null) {
		        throw new IllegalArgumentException("Network Manager should not be null.");
		    }
		    if (this.networkManager != null) {
		    	Logg.warning("Network Manager already set.");
		    }
			this.networkManager = networkManager;
			return this;
		}

		/**
		 * Sets Decoder which will be use for decoding image stream.<br />
		 * Default - {@link com.freedom.asyncimageloader.assist.BitmapDecoder BitmapDecoder}
		 */
		public OptionsBuilder setimageDecoder(Decoder decoder) {
			if (decoder == null) {
		        throw new IllegalArgumentException("Image Decoder should not be null.");
		    }
		    if (this.imageDecoder != null) {
		    	Logg.warning("Image Decoder already set.");
		    }
			this.imageDecoder = decoder;
			return this;
		}

		public OptionsBuilder renewWithOld(LoaderOptions options) {
			allowWebsites = options.allowWebsites;
			isCheckValidUrlBeforeLoading = options.isCheckValidUrlBeforeLoading;
			waitForUrlBeforeLoading = options.waitForUrlBeforeLoading;
			imageValidator = options.imageValidator;
			httpSocket = options.httpSocket;
			context = options.context;
			uriControl = options.uriControl;
			loggingEnabled = options.loggingEnabled;
			downloader = options.downloader;
			cacheBitmapTransform = options.cacheBitmapTransform;
			cacheDiscBitmapTransform = options.cacheDiscBitmapTransform;
			removeBackground = options.removeBackground;
			memoryCache = options.memoryCache;
			defaultMemoryCache = options.defaultMemoryCache;
			memoryCacheListener = options.memoryCacheListener;
			allowMultiplekeyCache = options.allowMultiplekeyCache;
			discCache = options.discCache;
			imageDecoder = options.imageDecoder;
			decodeFileQuality = options.decodeFileQuality;
			networkManager = options.networkManager;
			discDirectory = options.discDirectory;
			discCacheDuration = options.discCacheDuration;
			discMemoryCacheOptions = options.discMemoryCacheOptions;
			defineEachActivity = options.defineEachActivity;
			multiCallbackEnabled = options.multiCallbackEnabled;
			resetBeforeLoading = options.resetBeforeLoading;
			checkNetworkBeforeLoading = options.checkNetworkBeforeLoading;
			enableSmoothLoading = options.enableSmoothLoading;
			fixSomeError = options.fixSomeError;
			makeLoaderFastOnCreate = options.makeLoaderFastOnCreate;
			useDeviceScreenSize = options.useDeviceScreenSize;
			initExecutor = options.initExecutor;
			fatchExecutor = options.fatchExecutor;
			cacheTaskExecutor = options.cacheTaskExecutor;
			threadPoolSize = options.threadPoolSize;
			removeQueryFromUrl = options.removeQueryFromUrl;
			enabledQuery = options.enabledQuery;
			threadPriority = options.threadPriority;
			AsyncTaskLoader = options.AsyncTaskLoader;
			defaultScaleType = options.defaultScaleType;
			discCahceFileNameExtension = options.discCahceFileNameExtension;
			discFileNameGenerator = options.discFileNameGenerator;
			deleteDiscCacheFileOlder = options.deleteDiscCacheFileOlder;
			discWriter = options.discWriter;
			imageStream = options.imageStream;
			defaultHandler = options.defaultHandler;
			return this;
		}
		
		/** Sets Empty Config */
		private void emptyUtils(Context context) {
			if (discMemoryCacheOptions == null) {
				discMemoryCacheOptions = new DiscMemoryCacheOptions()
				.targetSizeForMemoryCache(0,0)
				//.targetOptionsForDiscCache(500,600)
				//.memoryCacheDuration(1000)
				//.discCacheDuration(CacheDuration.MONTH.ONE_MONTH)
				;
			}
			
			if (imageValidator == null) {
				imageValidator = new ImageUrlValidator();
			}
			
			if (imageStream == null) {
			    imageStream = Utility.createImageStream(context,networkManager);
			}
			
			if (removeBackground && imageStream != null)
				imageStream.bitmapCompressFormat(Bitmap.CompressFormat.PNG);
			
			if (downloader == null) {
				downloader = Utility.createImageDownloader(context,imageStream);
			}
			
			if (discFileNameGenerator == null) {
			    discFileNameGenerator = Utility.createFileNameGenerator();
			}
			
			if (discWriter == null) {
			    discWriter = new DefaultDiscWriter();
			}
			
			if (defaultMemoryCache == null) {
				if(discMemoryCacheOptions.maxMemoryCacheSize != 0) {
					defaultMemoryCache = Utility.createLimitedMemoryCache(discMemoryCacheOptions.maxMemoryCacheSize);
				}else {
					defaultMemoryCache = Utility.createMemoryCache(discMemoryCacheOptions.maxMemoryCacheSize);
				}
			}

            if (discCache == null) {
				if(discMemoryCacheOptions.maxDiscCacheSize != 0) {
					discCache = Utility.createSizeDiskCache(context,discMemoryCacheOptions.maxDiscCacheSize);
				}else {
					discCache = Utility.createDiskCache(context);
				}
			}
		    
		     if (fatchExecutor == null) {
				if(executorOptions != null) {
					ExecutorOptions executor = executorOptions;
					fatchExecutor = Utility.createExecutor(executor.threadPoolSize,executor.threadPriority,
							executor.keepAlive,executor.timeUnit,executor.taskQueue);
	            }else {
	            	fatchExecutor = Utility.createExecutor(threadPoolSize,threadPriority);
	            }
			} else {
				initExecutor = true;
			}

			if (cacheTaskExecutor == null) {
				if(executorOptions != null) {
					ExecutorOptions executor = executorOptions;
					cacheTaskExecutor = Utility.createExecutor(executor.threadPoolSize,executor.threadPriority,
							executor.keepAlive,executor.timeUnit,executor.taskQueue);
	            }else {
					cacheTaskExecutor = Utility.createExecutor(threadPoolSize,threadPriority);
	            }
			} else {
				initExecutor = true;
			}
	
			if (imageDecoder == null) {
				imageDecoder = Utility.createBitmapDecoder();
			}
			
			if (networkManager == null) {
				networkManager = Utility.createNetworkService(context);
			}
			
			if (discDirectory == null) {
		        //Create cache directory to save cached images
				discDirectory = Utility.createDefaultDiscCacheDirectory(context);
			}

			discCache.init(discDirectory, discFileNameGenerator);
			memoryCache = new MemoryCacheOpen(discCache,defaultMemoryCache,memoryCacheListener);
			networkManager.setHttpSocket(httpSocket);
		}

		/** Builds Controller Config results */
		public LoaderOptions build() {
			emptyUtils(context);
			return new LoaderOptions(this);
		}
	}
}
