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

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Executor;

import com.freedom.asyncimageloader.interfaces.PhotoLoadFrom;
import com.freedom.asyncimageloader.interfaces.DataEmiter;
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
import com.freedom.asyncimageloader.uri.UriRequest;
import com.freedom.asyncimageloader.uri.URLEncoded.UriScheme;
import com.freedom.asyncimageloader.utils.Logg;
import com.freedom.asyncimageloader.utils.Utility;
import com.freedom.asyncimageloader.listener.*;
import com.freedom.asyncimageloader.callback.*;

public class AsyncTaskLoader extends AsyncTask<String, Void, Bitmap>
		implements PhotoTask, CusListener.failedListener, CusListener.BytesListener {

	private final int IO_BUFFER_SIZE = 32 * 1024;
	private final LoaderManager manager;

	TaskStatus taskStatus;
	Bitmap bitmap;
	final String uri;
	final ImageWrapper imageWrapper;
	final RequestData data;
	final DiscCache discCache;
	final Downloader downloader;
	final MemoryCache memoryCache;
	final LoaderOptions options;
	final String cacheKey;
	final Decoder imageDecoder;
	boolean taskPaused = false;
	final boolean localDownloading;
	final Handler handler;

	private boolean saveScaledImageOnDisc = false;
	private boolean canDecodeBitmap = true;
	DataEmiter dataEmiter = new DataEmiter();
	final LoaderCallback cb;
	final ProgressCallback pcb;

	public AsyncTaskLoader(String UriName,LoaderManager manager, RequestData data,LoaderCallback cb, ProgressCallback pcb, LoaderOptions options,
						   ImageWrapper imageWrapper,boolean localDownloading) {
		this.options = options;
		this.manager = manager;
		this.data = data;
		this.imageDecoder = options.getImageDecoder();
		this.memoryCache = options.getMemoryCache();
		this.discCache = options.getDiscCache();
		this.downloader = options.getDownloader();
		this.localDownloading = localDownloading;
		this.handler = data.setting.getHandler();
		this.imageWrapper = imageWrapper;
		this.cacheKey = data.getMemoryCacheKey();
		this.uri  = UriName;
		this.cb = cb;
		this.pcb = pcb;
		dataEmiter = data.dataEmiter;
		dataEmiter.loadedFrom = PhotoLoadFrom.NETWORK;
	}

	/**
	 * Runs on the UI thread before {@link #doInBackground}.
	 *
	 * @see #onPostExecute
	 * @see #doInBackground
	 */
	@Override
	protected void onPreExecute() {
		Logg.info("loading image from " + uri);
	}

	/**
	 * execute asyncTask using executor
	 *
	 * @param executor executor use to execute task
	 */
	public void execute(final Executor executor) {
		if (Build.VERSION.SDK_INT < 11) {
			execute();
		} else {
			executeTaskHoneycomb(executor);
		}
	}

	/**
	 * execute asyncTask using executor
	 *
	 * @param executor executor use to execute task
	 */
	@TargetApi(11)
	private void executeTaskHoneycomb(final Executor executor) {
		executeOnExecutor(executor);
	}

	/**
	 * start loading AsyncTask
	 *
	 * @see #onPostExecute
	 * @see #onBytesUpdate
	 */
	@Override
	protected Bitmap doInBackground(String... args) {
		/*Bitmap bitmap = null;
		pauseTaskIfSet();
		cleanOlderCache(data, options.discCacheDuration);
		ReentrantLock cacheLockUri = data.checkLockUri;
		final ArrayList<AsyncTaskLoader> actives = manager.getAsyncWaitingTasks().get(currentLoadingUri());
		final ArrayList<AsyncTaskLoader> downloads = new ArrayList<AsyncTaskLoader>();
		cacheLockUri.lock();
		try {
			if(actives != null && actives.size() != 0) {
				if (data.imageWrapper != null) {
					actives.add(this);
				}
				return null;
			}
			manager.getPendingDownloads().add(this);
			if (data.imageWrapper != null) {
				downloads.add(this);
			}
			manager.getAsyncWaitingTasks().put(currentLoadingUri(), downloads);
			checkActiveReused();
			bitmap = memoryCache.get(cacheKey);

			if (isBitmapInMemoryCache(bitmap)) {
				dataEmiter.loadedFrom = PhotoLoadFrom.CACHE;
			}
			checkActiveReused();
			if (bitmap == null) {
				File cacheFile = getDiscCacheFile(uri);
				bitmap = get(cacheFile);

				if (bitmap == null) {
					onFailed(FailMethod.UNKNOWN_ERROR, uri);
				}
			}
			if(saveScaledImageOnDisc && bitmap != null) {
				File cacheScaledFile = discCache.getFile(uri, "-" + data.size.width + "x" + data.size.height+options.discCahceFileNameExtension);
				options.discWriter.writeFile(cacheScaledFile,bitmap,options.discMemoryCacheOptions.bitmapCompressFormat,
											 options.discMemoryCacheOptions.discCompressQuality);
			}
			checkActiveReused();
		} catch (IOException e) {
			deleteFile(discCache.getFile(uri));// delete if exits before download
		} catch (CancelledTaskException e)  {
			onLoadCancel(StopMethod.INTERRUPTED_REUSED,"task cancelled",uri);
			return null;
		} finally {
			cacheLockUri.unlock();
			//manager.reloadAsyncTaskPendingDownload(uri);
			manager.getPendingDownloads().remove(this);
			manager.getAsyncWaitingTasks().remove(currentLoadingUri());
		}
		dataEmiter.infoValue = Utility.updateLoadingUsage(dataEmiter.startTime, bitmap);
		dataEmiter.loadedFrom = dataEmiter.loadedFrom;*/
		return bitmap;
	}

	protected Bitmap get(File cacheFile) throws CancelledTaskException, IOException {
		Bitmap bitmap = null;
		if (isCancelled()) {
			onLoadFailed(FailMethod.INTERRUPTED,"task cancelled",uri);
			return null;
		}
		if(cacheFile.exists()) {
			checkActiveReused();
			bitmap = decodeFile(UriScheme.FILE.drag(cacheFile.getAbsolutePath()),data.targetSize);
			if (bitmap == null) {
				deleteFile(cacheFile);// delete if exits before download
			}
		}
		if (bitmap != null) {
			bitmap = manager.loadMargeBitmapIfNecessary(data,data.setting.getBitmapDisplayer(),bitmap);
			if(data.setting.shouldCahceInMemory()) {
				memoryCache.put(cacheKey, bitmap);
			}
			dataEmiter.loadedFrom = PhotoLoadFrom.DISC;
		} else {
			/** from network. */
			boolean downloaded = downloadFile(data.getUriRequest(),cacheFile);
			if (downloaded) {
				checkActiveReused();
				decodedFile(cacheFile);
				/** Decodes image and scales it to reduce memory consumption. */
				bitmap = decodeFile(UriScheme.FILE.drag(cacheFile.getAbsolutePath()),data.targetSize);
				dataEmiter.loadedFrom = PhotoLoadFrom.NETWORK;
			}
			if (bitmap != null) {
				bitmap = manager.loadMargeBitmapIfNecessary(data,data.setting.getBitmapDisplayer(),bitmap);
				if(data.setting.shouldCahceInMemory()) {
					memoryCache.put(cacheKey, bitmap);
				}
				dataEmiter.loadedFrom = PhotoLoadFrom.NETWORK;
			}else {
				deleteFile(cacheFile);// delete if exits before download
			}
			checkActiveReused();
		}
		return bitmap;
	}
	/**
	 * Download a bitmap from a URL, write it to a disk and
	 * @return </br>true</br> if downloaded
	 *
	 * @param cacheFile The URL to fetch
	 */
	private boolean downloadFile(UriRequest request,File cacheFile) throws IOException {
		Logg.debug("loading bitmap stream - downloading - " + uri);
		InputStream downloadStream;
		boolean downloaded = false;
		if(manager.shouldUseNoNetwork()) {
			if(manager.networkUri(request.getUriString())) {
				throw new RequestDeniedException("local uri only allowed");
			}else {
				downloadStream = downloader.downloadStream(request,this);
			}
		} else if(manager.shouldUseOnlyNetwork()) {
			if(manager.networkUri(request.getUriString())) {
				downloadStream = downloader.downloadStream(request,this);
			}else {
				throw new RequestDeniedException("network url only allowed");
			}
		} else {
			if(manager.networkUri(request.getUriString()) && options.shouldCheckNetworkBeforeLoading()) {
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
	 * Runs on the UI thread.
	 * <p>This method can only be invoked when loading bytes.</p>
	 *
	 * @param current The current updating bytes and total size.
	 * @see #doInBackground
	 * @see #downloadFile
	 */
	@Override
	public boolean onBytesUpdate(int current, int total) {
		Logg.info("reading bytes - current: "+current +" total: "+total +" from uri - " + uri);
		return data.setting.progressCallback == null || onProgressUpdate(current, total);
	}

	/**
	 * Runs when downloading image from Internet
	 *
	 * <p>This method can only be invoked when download failed.</p>
	 */
	@Override
	public boolean onFailed(String url) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean pauseTaskIfSet() {
		if (!manager.isPause()) {
			return false;
		}
		synchronized (manager.mPauseLock()) {
			try {
				taskPaused = true;
				if (manager.getPausedTime() != 0) {
					pauseTaskTimeSet();
				}
				manager.mPauseLock().wait();
			} catch (Exception e) {
				Logg.debug("Pausing the thread error " + e.getMessage());
			}
		}
		return isImageViewReused() || isViewNotActive();
	}

	private void pauseTaskTimeSet() {
		synchronized (manager.mPauseLock()) {
			try {
				data.setting.getHandler().postDelayed(new Runnable() {
					@Override public void run() {
						manager.resume();
					}
				},manager.getPausedTime());
			} catch (Exception e) {
				Logg.debug("Pausing the thread error " + e.getMessage());
			}
		}
	}

	@Override
	public void cancel() {
		super.cancel(true);
	}

	/** quit incoming or uncompleted loading thread.*/
	public void quitThread() {
		taskStatus = TaskStatus.FINISHED;
		Thread.interrupted();
	}

	@Override
	protected void onCancelled() {}

	@Override
	public boolean isTaskCancelled() {
		return isCancelled();
	}

	public boolean cancelTask(boolean cancel) {
		return cancel(cancel);
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
	 * @throws IllegalStateException If {@link #getStatus()} returns either
	 */
	public boolean checkStatus() {
		switch (taskStatus) {
			case RUNNING:
				throw new IllegalStateException(SimpleTaskLoader.TASK_ALREADY_RUNNING);
			case FINISHED:
				throw new IllegalStateException(SimpleTaskLoader.TASK_ALREADY_FINISHED);
			case PAUSE:
				throw new IllegalStateException(SimpleTaskLoader.TASK_PAUSE_WAITING_TO_RESUME);
			default:
				break;
		}
		return true;
	}

	protected boolean onProgressUpdate(int bytes,int total) {
		if (isCancelled() || isImageViewReused()) return false;
		manager.callback.progress(data.setting,pcb,bytes, total);
		return true;
	}

	@Override
	protected void onPostExecute(Bitmap bitmap) {
		if (bitmap == null) {
			onLoadFailed(FailMethod.UNKNOWN_ERROR, "loading bitmap failed");
			return;
		}
		if (isCancelled()) {
			onLoadFailed(FailMethod.INTERRUPTED,"loading task cancelled",uri);
			bitmap.recycle();
			return;
		}
		if (bitmap != null) {
			onFinished(bitmap);
		}
	}

	public String currentLoadingUri() {
		return uri;
	}

	public LoaderCallback getCallback() {
		return cb;
	}

	public ProgressCallback getProgressCallback() {
		return pcb;
	}

	/**
	 * Returns the current cache key
	 *
	 * @return The current cache key
	 */
	public String getCacheKey() {
		return cacheKey;
	}

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
		return getDiscCacheFile(currentLoadingUri());
	}

	public File getDiscCacheFile(String uri) {
		File imageFile = null;
		if (data.setting.shouldCahceThumbnailOnDisc()) {
			final String extensionName  = "-" + data.size.width + "x" + data.size.height+options.discCahceFileNameExtension;
			imageFile = discCache.getFile(uri,extensionName);
			canDecodeBitmap = false;
		}
		if (imageFile == null || !imageFile.exists()) {
			imageFile = discCache.getFile(uri);
			if (data.setting.shouldCahceThumbnailOnDisc()) {
				Logg.info("image thumbnail on disc cache is allow");
				saveScaledImageOnDisc = true;
			}
			canDecodeBitmap = true;
		}
		return data.setting.skipDiscCache ? null : imageFile;// find image in disc cache
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

	void onLoadFailed(FailMethod fail, String msg) {
		if (isCancelled() || isImageViewReused()) return ;
		manager.callback.failed(data.setting,cb,new Failed(fail, msg+"dddd uri: "+uri),uri, imageWrapper.getView());
	}

	void onLoadFailed(FailMethod fail,String msg,String uri) {
		if (isCancelled() || isImageViewReused()) return ;
		manager.callback.failed(data.setting,cb,new Failed(fail, msg+"dddd uri: "+uri),uri, imageWrapper.getView());
	}

	Bitmap decodeFile(String key, Resizer targetSize) throws IOException {
		RequestForDecode decodings =  new RequestForDecode(key,canDecodeBitmap,targetSize, targetSize,
				manager.createBitmapOptions(data),data);
		return imageDecoder.decodeFile(decodings);
	}

	/**
	 * Clear discCache all cached images older than a set time.
	 */
	public void cleanOlderCache(final RequestData data,final long time) {
		if(time != 0 && options.discCache != null) {
			handler.post(new Runnable() {
				@Override public void run() {
					options.discCache.cleanCache(options.context,time);
				}});
		}
	}

	void checkActiveReused() throws CancelledTaskException {
		checkViewReused();
		checkViewNotActive();
	}

	void checkViewReused() throws CancelledTaskException {
		if(isImageViewReused())
			throw new CancelledTaskException();
	}

	void checkViewNotActive() throws CancelledTaskException {
		if(isViewNotActive())
			throw new CancelledTaskException();
	}

	boolean isImageViewReused() {
		String loadingCacheKey = manager.requestUriView(imageWrapper);
		boolean isImageWrapperReused = loadingCacheKey == null || !cacheKey.equals(loadingCacheKey);
		if(isImageWrapperReused) {
			return true;
		}
		return false;
	}

	boolean isViewNotActive() {
		return imageWrapper.isViewNotActive();
	}

	private void onFinished(final Bitmap bitmap) {
		dataEmiter.bitmap = bitmap;
		if (isCancelled()) {
			dataEmiter.bitmap = null;
		}

		handler.post(new Runnable() {
			@Override  public void run() {
				isBitmapSuccess(bitmap,new OkCallback() {
					@Override public void onResult(Object obj, boolean isOk) {
						if (isOk) {
							if (data.setting.getAnimation() != null) {
								data.setting.getBitmapDisplayer().onLoaded(data.setting,dataEmiter,imageWrapper,data.setting.imageOptions.rounded);
								manager.animateBitmap(data.setting.getAnimation(), dataEmiter, imageWrapper);
							} else {
								data.setting.getBitmapDisplayer().onLoaded(data.setting,dataEmiter,imageWrapper,data.setting.imageOptions.rounded);
							}
							manager.cancelRequestView(imageWrapper);
							manager.callback.loaded(data.setting,cb,dataEmiter,imageWrapper.getView());
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

	private boolean isBitmapInMemoryCache(Bitmap bitmap) {
		return bitmap != null;
		// return bitmap != null && !bitmap.isRecycled();
	}

	private void deleteFile(File cacheFile) {
		try {
			manager.delete(cacheFile);
		} catch (Exception e) {
			//Do nothing
		}
	}

	public void isBitmapSuccess(Bitmap bitmap, OkCallback ok) {
		if (imageWrapper.isViewNotActive() || isImageViewReused())  {
			log_d(isImageViewReused() ? "imageView Reused" : "image view not active");
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

	protected void decodedFile(File cacheFile) throws IOException {
		if(options.shouldCacheDiscMaxSizes()) {
			int masDiscWidth = options.discMemoryCacheOptions.maxWidthForDisc;
			int masDiscHeight = options.discMemoryCacheOptions.maxHeightForDisc;
			Resizer target = new Resizer(masDiscWidth,masDiscHeight);
			RequestForDecode decoding =  new RequestForDecode(uri,true, target, target,null,data);
			imageDecoder.saveImage(decoding,options.discMemoryCacheOptions);
		}
	}

	private void log_d(String message) {
		if (options.loggingEnabled) Logg.debug(message);
	}
}