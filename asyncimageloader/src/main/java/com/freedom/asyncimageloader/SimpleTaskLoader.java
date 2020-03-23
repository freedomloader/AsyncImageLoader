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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.locks.ReentrantLock;

import com.freedom.asyncimageloader.assist.Decoder;
import com.freedom.asyncimageloader.assist.Resizer;
import com.freedom.asyncimageloader.assist.BitmapDecoder.RequestForDecode;
import com.freedom.asyncimageloader.cache.DiscCache;
import com.freedom.asyncimageloader.cache.MemoryCache;
import com.freedom.asyncimageloader.download.Downloader;
import com.freedom.asyncimageloader.exception.CancelledTaskException;
import com.freedom.asyncimageloader.exception.RequestDeniedException;
import com.freedom.asyncimageloader.imagewrapper.ImageWrapper;
import com.freedom.asyncimageloader.interfaces.Failed;
import com.freedom.asyncimageloader.interfaces.Failed.FailMethod;
import com.freedom.asyncimageloader.listener.CusListener;
import com.freedom.asyncimageloader.listener.OkCallback;
import com.freedom.asyncimageloader.uri.URLEncoded;
import com.freedom.asyncimageloader.uri.UriRequest;
import com.freedom.asyncimageloader.uri.URLEncoded.UriScheme;
import com.freedom.asyncimageloader.utils.BitmapUtils;
import com.freedom.asyncimageloader.utils.Logg;
import com.freedom.asyncimageloader.utils.Utility;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.widget.*;
import com.freedom.asyncimageloader.interfaces.*;
import com.freedom.asyncimageloader.listener.*;
import com.freedom.asyncimageloader.callback.*;

import java.util.concurrent.*;

public final class SimpleTaskLoader extends FImgConstants implements Runnable, PhotoTask, CusListener.BytesListener,CusListener.failedListener {

	private final LoaderManager manager;
	private final Handler handler;
	final boolean logsEnabled;

	final ImageWrapper imageWrapper;
	final RequestData data;
	final DiscCache discCache;
	final MemoryCache memoryCache;
	final Downloader downloader;
	final LoaderOptions options;
	final ProgressCallback pcb;
	final LoaderCallback cb;
	String uri;
	final Decoder imageDecoder;
	final String cacheKey;

	private final DataEmiter dataEmiter;
	TaskStatus taskStatus;

	private boolean saveScaledImageOnDisc = false;
	private boolean canDecodeBitmap = true;
	private String cacheKeyUri;
	private boolean useDownloaderForInputStream = false;
	Bitmap bitmap = null;

	public SimpleTaskLoader(LoaderManager manager, RequestData data,LoaderCallback cb, ProgressCallback pcb, LoaderOptions options) {
		this.options = options;
		this.manager = manager;
		this.data = data;
		this.logsEnabled = options.loggingEnabled;
		this.handler = data.setting.getHandler();
		this.memoryCache= options.getMemoryCache();
		this.discCache = options.getDiscCache();
		this.downloader = options.getDownloader();
		this.imageDecoder = options.getImageDecoder();
		this.imageWrapper = data.getImageWrapper();
		this.cacheKey = data.memoryCacheKey;
		this.uri  = data.uriRequest.getUriString();

		this.dataEmiter = data.dataEmiter;
		this.dataEmiter.loadedFrom = PhotoLoadFrom.DISC;

		if (options.multiCallbackEnabled) {
			manager.callback.key(cacheKey);
		}
		this.cb = cb;
		this.pcb = pcb;
	}

	@Override
	public void execute(Executor executor) {
		executor.execute(this);
	}

	@Override
	public void cancel() {
		quitThread();
	}

	/** quit incoming or uncompleted loading thread.*/
	public void quitThread() {
		taskStatus = TaskStatus.FINISHED;
		Thread.interrupted();
	}

	@Override
	public boolean isTaskCancelled() {
		// TODO: Implement this method
		return false;
	}

	@Override public void run() {
		log_i(START_LOADING_IMAGE_WAITING_TO_LOADED_AND_DISPLAY + " uri: "+uri);
		taskStatus = TaskStatus.RUNNING;

		if (isAllUriShouldDelay()) return ;
		if (isThreadShouldPause()) return ;
		if (isUriShouldDelay()) return ;
		if (isUriShouldBlock()) return ;
		if (isUriShouldChange()) return ;
		ReentrantLock cacheLockUri = data.checkLockUri;

		cacheLockUri.lock();
		manager.getPendingDownloads().add(this);
		cleanOlderCache(data, options.discCacheDuration);

		try {
			checkReusedActiveInterrupted();

			if (!data.setting.skipMemoryCache) {
				bitmap = memoryCache.get(cacheKey);
			}

			if (bitmap == null) {
				bitmap = huntBitmap();

				if (bitmap == null)  {
					log_d("loading bitmap failed");
					return ;
				} else {
					checkReusedActiveInterrupted(true);

					if (data.imageOptions.blackAndWhite) {
						bitmap = BitmapUtils.toBlackandWhite(bitmap);
					} else if(data.imageOptions.grayScale) {
						bitmap = BitmapUtils.toGrayscale(bitmap);
					}

					if (data.setting.transform != null) {
						bitmap = data.setting.transform.transformBitmap(bitmap);
					}

					if (saveScaledImageOnDisc) {
						File cacheScaledFile = discCache.getFile(uri, "-" + data.size.width + "x" +
								data.size.height+options.discCahceFileNameExtension);

						options.discWriter.writeFile(cacheScaledFile,bitmap,getCompressFormat(bitmap),
								options.discMemoryCacheOptions.discCompressQuality);

						if (canDecodeBitmap) {
							log_i("image can be decoded cause thumbnail image is deny or not find");
						} else {
							log_i("image cant be decode cause thumbnail image is allow and find");
						}
					}
				}
			} else {
				dataEmiter.loadedFrom  = PhotoLoadFrom.CACHE;
				log_d(LOAD_IMAGE_FROM_MEMORY_CACHE);
			}
			bitmap = manager.loadMargeBitmapIfNecessary(data,data.setting.getBitmapDisplayer(),bitmap);
			/** check if bitmap should be cache.*/
			if(bitmap != null && shouldUseMemoryCache()) {
				/** cache bitmap to memory cache.*/
				memoryCache.put(cacheKey, bitmap);
				log_d(IMAGE_CACHED_IN_MEMORY);
			}
			checkReusedActiveInterrupted(true);
		} catch (OutOfMemoryError e) {
			StringWriter stringAppend = new StringWriter()
					.append(" outofmemory: ",IMAGE_OUT_OF_MEMORY)
					.append(" message: ",e.getMessage())
					.append(" memoryCache: ",memoryCache.toString()).flush();
			log_d(stringAppend.toString());
			onLoadFailed(FailMethod.OUT_OF_MEMORY,stringAppend.toString());
			memoryCache.onLowMemory();
		} catch (CancelledTaskException e) {
			log_d(TASK_THREAD_INTERRUPTED_WITH_URI);
			onLoadFailed(FailMethod.INTERRUPTED_REUSED, TASK_THREAD_INTERRUPTED_WITH_URI);
			return;
		} catch (IOException e) {
			log_i("failed when saving thumbnail image on disc");
		} finally {
			manager.getPendingDownloads().remove(this);
			manager.reloadSimpleStreamWaitingTask(uri);
			cacheLockUri.unlock();
		}
		isSuccess(bitmap,new OkCallback() {
			@Override
			public void onResult(Object obj, boolean isOk) {
				if(isOk) {
					dataEmiter.infoValue =
							Utility.updateLoadingUsage(dataEmiter.startTime, bitmap);
				}
				onFinished(bitmap);
			}
		});
	}

	private Bitmap huntBitmap() throws CancelledTaskException {
		Bitmap bitmap = null;
		log_d(LOADING_IMAGE_FROM_DISC);
		final File cacheFile = getDiscCacheFile();// find image in disc cache
		String fileKeyUri = UriScheme.FILE.drag(cacheFile != null ? cacheFile.getAbsolutePath() : "");

		try {
			if (options.isQueryEnabled() && data.uriRequest != null) {
				URLEncoded mencode = URLEncoded.parse(data.uriRequest.getUriName());
				if (mencode.hasQuery()) {
					data.uriRequest.uri = Uri.parse(mencode.removeQuery());
				}
			}
			if (cacheFile.exists() && !data.setting.skipDiscCache)  {

				checkReusedActiveInterrupted();
				bitmap = decodeFile(fileKeyUri,data.targetSize,false);// decode bitmap

				isSuccess(bitmap,new OkCallback() {
					@Override
					public void onResult(Object obj, boolean isOk) {
						if (isOk) {
							log_i(LOAD_IMAGE_FROM_DISC_CACHE);
							dataEmiter.loadedFrom = PhotoLoadFrom.DISC;
							log_i(DECODE_IMAGE_FILE);
						} else if(shouldFixSomeError()) {
							manager.delete(cacheFile);// delete because decoding failed
						}
					}
				});
			}
			cacheKeyUri = fileKeyUri;
			if (bitmap == null) {
				log_d(LOADING_IMAGE_FROM_NETWORK);

				if(shouldCacheOnDisc()) {
					cacheKeyUri = loadBitmap(cacheFile, uri) ? fileKeyUri :
							data.setting.skipDiscCache ? !shouldCacheOnDisc() ? uri : "freedom-skip-cache" : uri; //download file
					useDownloaderForInputStream = false;
				} else {
					cacheKeyUri = uri;
					useDownloaderForInputStream = true;
				}

				checkReusedActiveInterrupted();
				bitmap = decodeFile(cacheKeyUri,data.targetSize,useDownloaderForInputStream);// decode bitmap

				isSuccess(bitmap,new OkCallback() {
					@Override public void onResult(Object obj, boolean isOk) {
						if (isOk) {
							dataEmiter.loadedFrom = PhotoLoadFrom.NETWORK;
							log_i(LOAD_IMAGE_FROM_NETWORK);
						} else if(shouldFixSomeError()) {
							delete(cacheFile);// delete because decoding failed
						}
					}
				});
			}
		} catch (CancelledTaskException e) {
			throw e;
		} catch (IOException e) {
			log_e(e,e.getMessage());
			StringWriter stringAppend = new StringWriter()
					.append(" decoding: ",IMAGE_DECODING_FAILED)
					.append(" message: ",e.getMessage()+" decoding failed "+uri)
					.append(" memoryCache: ",memoryCache.toString()).flush();
			log_d(stringAppend.toString());
			onLoadFailed(FailMethod.IMAGE_DECODE_FAILED,stringAppend.toString());
			/** failed check if exists if true then delete the file */
			delete(cacheFile);
		} catch (Throwable e) {
			log_e(e,e.getMessage());
			onLoadFailed(FailMethod.UNKNOWN_ERROR,e.getMessage()+" "+IMAGE_FAIL_TO_LOAD);
		}
		return bitmap;
	}

	private boolean loadBitmap(File cacheFile,String uri) throws CancelledTaskException {
		/** load image using URLEncoded.*/
		boolean downloaded = false;
		try {
			if (networkUri(data.uriRequest.toString())) {
				manager.setSimpleStreamWaitingTask(data.uriRequest.toString(),null,true);
			}
			/** if </b>true</b> file download successful </b>false</b> getting stream failed*/
			downloaded = loadStream(data.getUriRequest(),cacheFile); // TODO : process boolean result
			/** Check </b>true</b> streamDownloaded complete*/
			if (cacheFile.exists() && downloaded == true) {

				if (options.shouldCacheDiscMaxSizes()) {
					Resizer target = new Resizer(options.discMemoryCacheOptions.maxWidthForDisc,
							options.discMemoryCacheOptions.maxHeightForDisc);
					RequestForDecode decoding =  new RequestForDecode(uri,true, target, target,null, data);

					Bitmap bmp = imageDecoder.decodeFile(decoding);
					boolean successful = options.discWriter.writeFile(cacheFile,bmp,getCompressFormat(bmp)
							,options.discMemoryCacheOptions.discCompressQuality);

					/** Check if </b>true</b> should modified file*/
					if (data.setting.shouldModified() && successful)
						manager.updateLastModifiedForCache(cacheFile);
				}
				discCache.put(uri, cacheFile);
			}
		} catch (RequestDeniedException e) {
			StringWriter stringAppend = new StringWriter()
					.append(" Request: ","Request denied")
					.append(" message: ",e.getMessage())
					.flush();
			onLoadFailed(FailMethod.REQUEST_DENIED,stringAppend.toString());
			log_e(e,e.getMessage());
			return downloaded;
		} catch (IOException e) {
			StringWriter stringAppend = new StringWriter()
					.append(" un known: ",IMAGE_FAIL_TO_LOAD)
					.append(" message: ","Error in download Bitmap - " + uri + "-"+ e.getMessage())
					.append(" memoryCache: ",memoryCache.toString()).flush();
			log_d(stringAppend.toString());
			onLoadFailed(FailMethod.UNKNOWN_ERROR,stringAppend.toString());
			/** failed check if exists if true then delete the file */
			delete(cacheFile);
			log_e(e,LOADER_SOME_ERROR_EXCEPTION +" Error in download Bitmap - " + uri + "-"+ e.getMessage());
		}
		return downloaded;
	}

	/**
	 * Get bitmap compress format {@link android.graphics.Bitmap.CompressFormat}
	 * for decoding bitmap
	 */
	public Bitmap.CompressFormat getCompressFormat(Bitmap bitmap) {
		final Bitmap.CompressFormat format;
		if (options.discMemoryCacheOptions.bitmapCompressFormat != null) {
			format = options.discMemoryCacheOptions.bitmapCompressFormat;
		} else {
			if (bitmap.getConfig() == Bitmap.Config.RGB_565 || !bitmap.hasAlpha()) {
				format = Bitmap.CompressFormat.JPEG;
			} else {
				format = Bitmap.CompressFormat.PNG;
			}
		}
		return format;
	}

	public Bitmap decodeFile(String key, Resizer targetSize, boolean useDownloaderForInputStream) throws IOException {
		RequestForDecode decodRequest =  new RequestForDecode(key,useDownloaderForInputStream,canDecodeBitmap,
				targetSize, targetSize, manager.createBitmapOptions(data),data);
		return imageDecoder.decodeFile(decodRequest);
	}

	/**
	 * <b>NOTE:</b> If you have set to use no network then method {#setDownloader(downloader)}
	 * in LoaderOptions {@link com.freedom.asyncimageloader.LoaderOptions OptionsBuilder}
	 * will not be consider
	 *
	 * if use only network images will not be loaded from disc and local.<br />.
	 * <b>NOTE:</b> If you set to use only network for loading images memory cache should be consider
	 */
	public boolean loadStream(final UriRequest request,File cacheFile) throws IOException {
		InputStream downloadStream;
		boolean downloaded = false;
		if(manager.shouldUseNoNetwork()) {
			if(networkUri(request.getUriString())) {
				throw new RequestDeniedException(NETWORK_DENIED);
			}else {
				downloadStream = downloader.downloadStream(request,this);
			}
		} else if(manager.shouldUseOnlyNetwork()) {
			if(networkUri(request.getUriString())) {
				downloadStream = downloader.downloadStream(request,this);
			}else {
				throw new RequestDeniedException(LOCAL_DENIED);
			}
		} else {
			if(networkUri(request.getUriString()) && options.shouldCheckNetworkBeforeLoading()) {
				if(manager.hasConnection())
					downloadStream = downloader.downloadStream(request,this);
				else
					downloadStream = null;
			}else {
				downloadStream = downloader.downloadStream(request,this);
			}
		}
		try {
			OutputStream imageOutput = new BufferedOutputStream(new FileOutputStream(cacheFile), IO_BUFFER_SIZE);
			try {
				downloaded = options.discWriter.writeOutput(downloadStream, imageOutput, this);// TODO : process boolean result
			} finally {
				Utility.closeQuietly(imageOutput);
			}
		} finally {
			Utility.closeQuietly(downloadStream);
		}
		return downloaded;
	}

	/**
	 * @return <b>true</b> - if all URLEncoded is delay; <b>false</b> - else continue
	 * loading task
	 *
	 * @see #isLoadReusedActive()
	 */
	public boolean isAllUriShouldDelay() {
		if (options.uriControl != null && options.uriControl.delayAllUri != 0) {
			try {
				if (options.uriControl.delayAllUri != 0) {
					taskStatus = TaskStatus.PENDING;
					Thread.sleep(options.uriControl.delayAllUri);
				}
			} catch (InterruptedException e) {
				log_d(TASK_THREAD_INTERRUPTED_WITH_URI + uri);
				return true;
			}
			return isLoadReusedActive();
		}
		return false;
	}

	/**
	 * Waits if necessary for at most the given time for current URLEncoded
	 *  timeout Time to wait before continue the task operation. show delay holder if set.
	 *
	 * @throws InterruptedException If the current thread was interrupted
	 *         while waiting.
	 *
	 * @see #isLoadReusedActive()
	 * @return {@link #isLoadReusedActive()}
	 */
	private boolean isUriShouldDelay() {
		if (options.uriControl != null && manager.isUriDelay(uri)) {
			log_d(DELAY_URI_BEFORE_LOADING + uri);
			synchronized (manager.mDelayLock()) {
				if(manager.uriDelayMillis(uri) != 0 ) {
					taskStatus = TaskStatus.PENDING;
					try {
						if(options.uriControl.delayHolder != 0) {
							showBitmap(imageWrapper,BitmapFactory.decodeResource(options.context.getResources(), options.uriControl.delayHolder));
						}
						manager.mDelayLock().wait(manager.uriDelayMillis(uri));
						manager.removeDelayUri(uri);
					} catch (InterruptedException e) {
						log_d(TASK_THREAD_INTERRUPTED_WITH_URI + uri);
						return true;
					}
					log_d(DELAY_URI_BEFORE_LOADING + uri);
				}
			}
		}
		return isLoadReusedActive();
	}

	/**
	 * Returns <tt>true</tt> if loading task should pause
	 * pause all loading task and incoming task.
	 *
	 * @see #isLoadReusedActive()
	 *
	 * @return {@link #isLoadReusedActive()}
	 */
	private boolean isThreadShouldPause() {
		if (!manager.isPause()) {
			return false;
		}
		synchronized (manager.mPauseLock()) {
			try {
				if (manager.getPausedTime() != 0) {
					pauseTaskTimeSet();
					manager.isPausedTime(true);
				}
				manager.mPauseLock().wait();onPause();
			} catch (InterruptedException e) {
				log_d("Pausing the thread error " + e.getMessage());
				return true;
			}
		}
		return isLoadReusedActive();
	}

	private void pauseTaskTimeSet() {
		synchronized (manager.mPauseLock()) {
			try {
				if (handler == null)
					return;
				handler.postDelayed(new Runnable() {
					@Override public void run() {
						manager.resume();
					}
				},manager.getPausedTime());
			} catch (Exception e) {
				log_d("Pausing the thread error " + e.getMessage());
			}
		}
	}

	/**
	 * Check if loading URLEncoded should be block
	 * {@link #isUriShouldBlock()} block current loading URLEncoded.
	 *
	 * @return <tt>true</tt> if URLEncoded should block
	 */
	private boolean isUriShouldBlock() {
		if (options.uriControl != null && manager.isUriBlock(uri)) {
			if(options.uriControl.blockHolder != 0) {
				showBitmap(imageWrapper,BitmapFactory.decodeResource(options.context.getResources(), options.uriControl.blockHolder));
			}
			log_d(BLOCK_URI_NOT_LOADING + uri);
			return true;
		}
		return isLoadReusedActive();
	}

	/**
	 * Check if loading URLEncoded should be change to another URLEncoded
	 * {@link #isUriShouldChange()} check to change loading URLEncoded.
	 *
	 * @see #isLoadReusedActive()
	 * @return <tt>true</tt> if Image View Reused
	 */
	private boolean isUriShouldChange() {
		if (options.uriControl != null && manager.isUriChange().containsKey(uri)) {
			synchronized (manager.mChangeLock()) {
				try {
					uri = manager.isUriChange().get(uri);
					manager.mChangeLock().wait(3);
				} catch (InterruptedException e) {
					log_d(TASK_THREAD_INTERRUPTED_WITH_URI + uri);
				}
			}
		} else {
			return false;
		}
		return isLoadReusedActive();
	}

	public boolean delete(File file) {
		// check if request to skip disc cache
		if(data.setting.skipDiscCache) {
			return false;//afford delete if failed to decode
		}
		return manager.delete(file);
	}

	/**
	 * Check if should cache image to memory cache
	 *
	 * Returns <tt>true</tt> - if should use cache for image
	 */
	public boolean shouldUseMemoryCache() {
		return data.setting.shouldCahceInMemory();
	}

	/**
	 * Check if should cache image to disc cache
	 *
	 * Returns <tt>true</tt> - if should cache image to disc
	 */
	public boolean shouldCacheOnDisc() {
		return data.setting.cacheOnDisc;
	}

	/**
	 * Check if smooth loading is enabled
	 *
	 * Returns <tt>true</tt> - if smooth loading is enabled
	 */
	public boolean smoothLoadingEnabled() {
		return options.shouldEnableSmoothLoading();
	}

	/**
	 * Check if Should Fix Some Error
	 *
	 * Returns <tt>true</tt> - if fix error is enabled
	 */
	public boolean shouldFixSomeError() {
		return options.shouldFixSomeError();
	}

	public File getDiscCacheFile() {
		File imageFile = null;
		if (discCache == null) {
			return null;
		}

		if (data.setting.shouldCahceThumbnailOnDisc()) {
			final String extensionName  = "-" + data.size.width + "x" + data.size.height+options.discCahceFileNameExtension;
			imageFile = discCache.getFile(uri,extensionName);
			canDecodeBitmap = false;
		}
		if (imageFile == null || !imageFile.exists()) {
			imageFile = discCache.getFile(uri,options.discCahceFileNameExtension);
			if (data.setting.shouldCahceThumbnailOnDisc()) {
				log_i("image thumbnail on disc cache is allow");
				saveScaledImageOnDisc = true;
			}
			canDecodeBitmap = true;
		}
		if (imageFile == null || !imageFile.exists()) {
			File cacheDir = imageFile.getParentFile();
			if (cacheDir == null || (!cacheDir.exists() && !cacheDir.mkdirs())) {
				cacheDir = discCache.getStorage();
				if (cacheDir != null && !cacheDir.exists()) {
					cacheDir.mkdirs();
				}
				if (cacheDir != null) {
					discCache.init(cacheDir, options.discFileNameGenerator);
					imageFile = discCache.getFile(uri,options.discCahceFileNameExtension);
				}
			}
		}
		return imageFile;// find image in disc cache
	}
	//
	/**
	 * Runs when reading file bytes to disc cache
	 *
	 * <p>This method can only be invoked when loading bytes.</p>
	 */
	@Override
	public boolean onBytesUpdate(int current,int total) {
		log_d("reading bytes - current: "+current +" total: "+total +" from uri - " + uri);
		//if progress callback null then return true
		return pcb == null || onProgressUpdate(current, total);
	}

	/**
	 * Runs when downloading image from network
	 *
	 * <p>This method can only be invoked when downloading failed.</p>
	 */
	@Override
	public boolean onFailed(String uri) {
		log_d("blocking uri - " + uri);
		options.uriControl.setBlockUri(uri);
		//options.uriControl.blockHolder(data.setting.getImageLoadingHolder(options.getResources()));
		if(options.uriControl.cacheKeysForBlockUri.containsKey(uri)) {
			log_d("uri - " + uri+" blocked");
		}else {
			log_d("blocking: " + "uri - " + uri +" failed");
		}
		onLoadFailed(FailMethod.SERVICE_FAILED,IMAGE_LOADING_FAILED+ " uri - " + uri);
		return true;
	}

	/**
	 * Runs when loading tasks paused {#manager.setPauseWork(true, time)} is invoked.
	 *
	 * <p>This method can only be invoked when task pause.</p>
	 * @see #isThreadShouldPause()
	 */
	private void onPause() {
		taskStatus = TaskStatus.PAUSE;
		log_d(TASK_PAUSE_WAITING_TO_RESUME + uri);
		log_d(PAUSE_LOADING_THREAD_TILL_WHEN_RESUME_CALL + uri);
	}

	/**
	 * Runs when paused task resume {#manager.resume()} is invoked.
	 *
	 * <p>This method can only be invoked when task resume.</p>
	 */
	void onResume() {
		taskStatus = TaskStatus.RUNNING;
		log_d(PAUSE_TASK_HAS_BEEN_RESUME);
	}

	/**
	 * Runs when loading Finished.
	 *
	 * <p>This method can only be invoked when loading Finished.</p>
	 */
	private void onFinished(final Bitmap bitmap) {
		/**
		 * load and display bitmap
		 * Causes the Runnable bitmapDisplayer (BitmapDisplayer) to be added to the handler.
		 * The runnable will be run on the thread with handler.
		 */
		dataEmiter.bitmap = bitmap;
		dataEmiter.targetSize = data.targetSize;
		taskStatus = TaskStatus.FINISHED;

		handler.post(new Runnable() {
			@Override
			public void run() {
				isBitmapSuccess(bitmap,new OkCallback() {
					@Override public void onResult(Object obj, boolean isLoaded) {
						if (isLoaded) {
							Bitmap transform = null;
							transform = data.setting.getBitmapDisplayer().onLoaded(data.setting,dataEmiter,imageWrapper,data.setting.imageOptions.rounded);
							if (data.setting.getAnimation() != null) {
								manager.animateBitmap(data.setting.getAnimation(), dataEmiter, imageWrapper);
							}
							manager.cancelRequestView(imageWrapper);
							if (transform != null)
								dataEmiter.bitmap = transform;
							onLoadBitmap(data.setting,dataEmiter,imageWrapper.getView());
							log_d("Bitmap load");
						} else {
							log_d("Bitmap load error");
							if(obj == null)
								onLoadFailed(FailMethod.UNKNOWN_ERROR,"Bitmap load error");
						}
					}});
			}
		});
	}

	/**
	 * Returns <tt>true</tt> - if images should be loaded from Internet
	 */
	public boolean networkUri(String uri) {
		switch (UriScheme.match(uri)) {
			case HTTP: case HTTPS:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Returns <tt>true</tt> - if status is pending
	 *
	 * @throws IllegalStateException If {#getStatus()} returns either
	 *         {PhotoLoadingTask.Status#RUNNING} or {PhotoLoadingTask.Status#FINISHED}.
	 */
	public boolean checkStatus() {
		switch (taskStatus) {
			case RUNNING:
				throw new IllegalStateException(TASK_ALREADY_RUNNING);
			case FINISHED:
				throw new IllegalStateException(TASK_ALREADY_FINISHED);
			case PAUSE:
				throw new IllegalStateException(TASK_PAUSE_WAITING_TO_RESUME);
			default:
				break;
		}
		return true;
	}

	/**
	 * Clear discCache all cached images older than a set time.
	 */
	public void cleanOlderCache(final RequestData data,final long time) {
		if(time != 0) {
			handler.post(new Runnable() {
				@Override public void run() {
					if (options.discCache != null) {
						options.discCache.cleanCache(options.context,time);
					}
				}});
		}
	}

	public LoaderCallback getCallback() {
		return cb;
	}

	public ProgressCallback getProgressCallback() {
		return pcb;
	}

	/**
	 * Returns the current status of this task.
	 *
	 * @return The current task status.
	 */
	public TaskStatus getTaskStatus() {
		return taskStatus;
	}

	/**
	 * Returns bitmap.
	 *
	 * @return bitmap.
	 */
	public Bitmap getBitmap() {
		return bitmap;
	}

	/**
	 * Returns the current loading URLEncoded.
	 *
	 * @return The current loading URLEncoded.
	 */
	public String currentLoadingUri() {
		return uri;
	}

	/**
	 * Returns the current cache key
	 *
	 * @return The current cache key
	 */
	public String getCacheKey() {
		return cacheKey;
	}

	/**
	 * Returns the current request data.
	 *
	 * @return The current loading data.
	 */
	public RequestData getData() {
		return data;
	}

	/**
	 * Returns where the image Loaded from.
	 *
	 * @return loadedFrom.
	 */
	public PhotoLoadFrom getLoadedFrom() {
		return dataEmiter.loadedFrom;
	}

	/**
	 * @throws CancelledTaskException if loading task is reused or ImageWrapper is reused for another task or the loading URLEncoded of
	 * this current task doesn't match to the URLEncoded which is active in ImageWrapper
	 */
	private void checkReusedActiveInterrupted() throws CancelledTaskException {
		checkReusedActiveInterrupted(false);
	}

	/**
	 * @throws CancelledTaskException if task was Interrupted
	 *     if loading task is reused or ImageWrapper is reused for another task or the loading URLEncoded of
	 *     this current task doesn't match to the URLEncoded which is active in ImageWrapper
	 */
	private void checkReusedActiveInterrupted(boolean shouldCheckTaskInterrupted) throws CancelledTaskException {
		/**
		 * Returns <tt>true</tt> - if ImageView is not active
		 *
		 * @throws CancelledTaskException if ImageView is not active
		 */
		if (isViewNotActive()) {
			throw new CancelledTaskException();
		}
		/**
		 * Returns <tt>true</tt> - if ImageWrapper is reused for another task then cancel current task
		 * @see #isLoadReusedActive()
		 * @throws CancelledTaskException if ImageView is reused for another task
		 */
		if(imageViewReused()){
			throw new CancelledTaskException();
		}

		/**
		 * Returns <tt>true</tt> - if task is Interrupted
		 *
		 * @throws CancelledTaskException if loading task is Interrupted
		 */
		if(shouldCheckTaskInterrupted) {
			if (isLoadingTaskInterrupted()) {
				onCancelled();
				throw new CancelledTaskException();
			}
		}
	}

	/**
	 * Returns <tt>true</tt> - if ImageWrapper is reused for another task or the loading URLEncoded of
	 *     this current task doesn't match to the URLEncoded which is active
	 */
	private boolean isLoadReusedActive() {
		return isViewNotActive() ||
				imageViewReused();
	}

	/**
	 * Runs when {@link #isLoadingTaskInterrupted()} is invoked and return true.
	 *
	 * @see #isLoadingTaskInterrupted()
	 * @see #isCancelled()
	 */
	protected void onCancelled() {
		taskStatus = TaskStatus.FINISHED;
		log_e(null,TASK_WAS_CANCELLED +"uri: "+uri);
		log_e(null,TASK_WAS_INTERRUPTED +"uri: "+uri);
	}

	/**
	 * is cancelled method normally.
	 *
	 * @return <tt>true</tt> if task was cancelled before it completed
	 * @see #checkReusedActiveInterrupted
	 */
	final boolean isCancelled() {
		return manager.isCacheExecutorShutdown()
				|| manager.isLoadingExecutorShutdown();
	}

	final boolean isCancelled(ImageWrapper imageWrapper) {
		return manager.isCancelled(imageWrapper);
	}

	/**
	 * <p>Runs when loading failed {#manager.onFailed}. The
	 * specified Failed Method is returned by loading task {#FailMethod}.</p>
	 *
	 * #manager.onFailed
	 * <p>This method can only be invoked if loading was failed.</p>
	 */
	private void onLoadFailed(FailMethod fail, String msg) {
		if (isLoadingTaskInterrupted()
				|| isLoadReusedActive())
			return;
		taskStatus = TaskStatus.FINISHED;
		manager.callback.failed(data.setting,cb,new Failed(fail, msg+" uri: "+uri),uri, imageWrapper.getView());
	}

	/**
	 * <p>Runs when bitmap loaded successful {#manager.loaded}. The
	 * specified DataEmiter is returned by loading task { #DataEmiter}.</p>
	 *
	 * #manager.loaded
	 * <p>This method can only be invoked if loading was successful.</p>
	 */
	private void onLoadBitmap(LoaderSettings settings,DataEmiter dataEmiter,ImageView view) {
		taskStatus = TaskStatus.FINISHED;
		manager.callback.loaded(settings,cb,dataEmiter,view);
	}

	/**
	 * This method is invoked from {onBytesUpdate(bytes, total)}
	 * to publish updates downloading update {@link #onProgressUpdate} on the UI
	 * thread.
	 *
	 * {@link #onProgressUpdate} will not be called if the task has been
	 * canceled only can be call when file is downloading and bytes is still reading.
	 *
	 * @param bytes current loading bytes
	 * @param total available size of downloading image
	 *
	 * @see #onBytesUpdate
	 */
	private boolean onProgressUpdate(int bytes,int total) {
		if (isLoadingTaskInterrupted()
				|| isLoadReusedActive()) return false;
		manager.callback.progress(data.setting,pcb,bytes, total);
		return true;
	}

	/**
	 * Returns <tt>true</tt> - if ImageWrapper is reused for another task then cancel current task
	 * @see #isLoadReusedActive()
	 * @see #imageViewReused()
	 * @see #checkReusedActiveInterrupted()
	 *
	 * Returns <tt>true</tt> - if image wrapper reused
	 */
	private boolean imageViewReused() {
		String loadCacheKey = manager.requestUriView(imageWrapper);
		boolean wrapperReused = !cacheKey.equals(loadCacheKey);
		if (wrapperReused) {
			log_e(null,TASK_IMAGEVIEW_REUSED);
			return true;
		}
		return false;
	}

	/**
	 * Returns <tt>true</tt> - if loading task is Interrupted
	 *
	 * Returns <tt>true</tt> - if task interrupted
	 */
	private boolean isLoadingTaskInterrupted() {
		if (Thread.interrupted()) {
			log_e(null,TASK_THREAD_INTERRUPTED_WITH_URI);
			return true;
		}
		return false;
	}

	/**
	 * Returns <tt>true</tt> - if ImageView is not active
	 *
	 * Returns <tt>true</tt> - if image view not active
	 */
	private boolean isViewNotActive() {
		if (imageWrapper.isViewNotActive()) {
			log_e(null,IMAGE_VIEW_NOT_ACTIVE);
			return true;
		}
		return false;
	}

	private void showBitmap(final ImageWrapper wrapper,final Bitmap bitmap) {
		handler.post(new Runnable() {
			@Override public void run() {
				taskStatus = TaskStatus.FINISHED;
				onFinished(bitmap);
			}});
	}

	private void log_d(String message) {
		if (logsEnabled) Logg.debug(message);
	}

	private void log_e(Throwable e,String message) {
		if (logsEnabled) Logg.error(e,message);
	}

	private void log_i(String message) {
		if (logsEnabled) Logg.info(message);
	}

	@Override
	public String toString() {
		String status = manager.isShutdown() ? "ImageLoader is shutdown" : manager.isPause() ? "ImageLoader is pause"
				: "ImageLoader is working OK";
		StringWriter stringAppend = new StringWriter()
				.append(" status: ",status)
				.append(" task status: ",taskStatus)
				.append(" memory cache: ",options.memoryCache.toString())
				.flush();
		return stringAppend.toString();
	}

	public void isSuccess(Object obj, OkCallback ok) {
		if (obj instanceof Bitmap) {
			Bitmap bitmap = (Bitmap) obj;
			if (bitmap == null || bitmap.getWidth() <= 0
					|| bitmap.getHeight() <= 0) {
				ok.onResult(obj, false);
			} else {
				ok.onResult(obj, true);
			}
		} else {
			if (obj != null) {
				ok.onResult(obj, false);
			} else {
				ok.onResult(obj, true);
			}
		}
	}

	public void isBitmapSuccess(Bitmap bitmap, OkCallback ok) {
		if (imageWrapper.isViewNotActive() || imageViewReused())  {
			log_d(imageViewReused() ? "imageView Reused" : "image view not active");
			manager.callback.failed(data.setting,cb,new Failed(FailMethod.VIEW_UNACTIVE,"image view not active"),
					uri, imageWrapper.getView());
			ok.onResult(bitmap, false);
		} else {
			if (bitmap != null) {
				ok.onResult(bitmap, true);
			} else {
				ok.onResult(bitmap, false);
			}
		}
	}
}