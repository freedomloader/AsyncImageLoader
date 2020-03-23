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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

import com.freedom.asyncimageloader.interfaces.DataEmiter;
import com.freedom.asyncimageloader.callback.CallType;
import com.freedom.asyncimageloader.callback.WorkerCallback;
import com.freedom.asyncimageloader.imagewrapper.ImageWrapper;
import com.freedom.asyncimageloader.imagewrapper.ViewTargetWrapper;
import com.freedom.asyncimageloader.interfaces.Failed;
import com.freedom.asyncimageloader.listener.LoaderCallback;
import com.freedom.asyncimageloader.uri.URLEncoded.UriScheme;
import com.freedom.asyncimageloader.utils.Logg;
import com.freedom.asyncimageloader.utils.TimeUtils;
import com.freedom.asyncimageloader.utils.Utility;
import com.freedom.asyncimageloader.download.*;
import com.freedom.asyncimageloader.callback.*;
import com.freedom.asyncimageloader.displayer.*;
import com.freedom.asyncimageloader.utils.*;
import com.freedom.asyncimageloader.assist.*;
import android.view.animation.*;
import com.freedom.asyncimageloader.interfaces.*;

public class LoaderManager {

	String URL_1 = "http://androiddesign2.freevar.com/android-im-chat/avatars/3IMG_20150221_102049.JPG";
	String URL_2 = "http://androiddesign2.freevar.com/android-im-chat/avatars/1IMG_20141216_155142.jpg";
	String URL_3 = "http://androiddesign2.freevar.com/android-im-chat/avatars/2IMG_20150128_150012.jpg";
	String URL_4 = "http://androiddesign2.freevar.com/android-im-chat/avatars/509425f6e3-117f-43d9-9b68-b7159368ab7d.jpg";
	private static final int TASK_SUBMIT = 1;
	private static final int CREATE_EXECUTOR = 2;
	private static final int CACHE_TIME_CLEAR = 3;
	private static final int TASK_CANCELLED = 4;
	private static final int TASK_DESTORY = 5;
	private static final int TASK_SHUTDOWN = 6;
	private static final int NETWORK_FAILED = 7;

	private static final String CHECKING_CONNECTION = "Checking Connection";
	private static final String CONNECTION_FOUND = "Connection found";
	private static final String CONNECTION_NOT_FOUND = "no network connection found";
	private static final String SUBMITTING_TASK = "Submitting Task";
	private static final String TASK_SUBMITED = "Task Submited";

	private Executor loadingTaskExecutor;
	private Executor cacheTaskExecutor;
	private Executor taskHunter;
	private LoaderOptions options;

	private final Object mLock = new Object();
	private final Object mPauseLock = new Object();
	private final Object mUriChangeLock = new Object();
	private final Object mUriDelayLock = new Object();
	private boolean mPauseWork = false;
	private int mPauseWorkTime = 0;
	private boolean isPausedTime = false;
	private boolean shutdown = false;
	private boolean useNoNetwork = false;
	private boolean useOnlyNetwork = false;
	final boolean loggingEnabled;
	private UriControlBuilder uriControl;
	LoaderSet loaderSet;
	WorkerCallback callback;
	private boolean singleCallbackEnabled = true;

	final Map<View, ViewTargetWrapper> requestTargetCache;
	public final Map<String, LoaderWorker> worker = new HashMap<String, LoaderWorker>();
	private final Map<Integer, String> cacheKeysForView = Collections.synchronizedMap(new HashMap<Integer, String>());

	private Map<String, Boolean> cacheKeysForBlockUri = Collections.synchronizedMap(new WeakHashMap<String, Boolean>());
	private Map<String, Integer> cacheKeysFordelayUri = Collections.synchronizedMap(new WeakHashMap<String, Integer>());
	private Map<String, String> cacheKeysForChangeUri = Collections.synchronizedMap(new WeakHashMap<String, String>());
	private Map<String, Boolean> cacheKeysForFailedUri = Collections.synchronizedMap(new WeakHashMap<String, Boolean>());
	private Map<String, String> cacheKeysForSimilarSize = Collections.synchronizedMap(new WeakHashMap<String, String>());
	private Map<String, List<PhotoTask>> mSimpleStreamWaitingTasks = Collections.synchronizedMap(new WeakHashMap<String, List<PhotoTask>>());
	private Map<String, Map<Integer, String>> margeBitmapCachePixel = Collections.synchronizedMap(new WeakHashMap<String, Map<Integer, String>>());

	private final Map<String, ReentrantLock> cacheLocksForUri = new WeakHashMap<String, ReentrantLock>();
	Hashtable<String, ArrayList<AsyncTaskLoader>> mAsyncWaitingTasks = new Hashtable<String, ArrayList<AsyncTaskLoader>>();
	LinkedHashSet<PhotoTask> mPendingDownloads = new LinkedHashSet<PhotoTask>();

	//public LoaderManager() {}
	public LoaderManager(LoaderOptions option) {
		this.options = option;
		loadingTaskExecutor = options.fatchExecutor;
		cacheTaskExecutor = options.cacheTaskExecutor;
		taskHunter = Executors.newCachedThreadPool();

		this.uriControl = options.uriControl;
		this.loggingEnabled = options.loggingEnabled;

		if(uriControl != null) {
			cacheKeysForBlockUri = uriControl.cacheKeysForBlockUri;
			cacheKeysFordelayUri = uriControl.cacheKeysForDelayUri;
			cacheKeysForChangeUri = uriControl.cacheKeysForChangeUri;
		}
		if(options.multiCallbackEnabled) {
			singleCallbackEnabled = false;
		} else {
			singleCallbackEnabled = true;
		}
		loaderSet = new LoaderSet(this,options);
		callback = new WorkerCallback(this,singleCallbackEnabled);
		this.requestTargetCache = new WeakHashMap<View, ViewTargetWrapper>();
		options.downloader.setManager(this);
	}

	public Bitmap loadMargeBitmapIfNecessary(RequestData data, TransformDisplayer bitmapDisplayer, Bitmap bitmap) {
		if (data.setting.shouldMargeBitmap()) {
			Bitmap roundBitmap = bitmapDisplayer. transformBitmap(data.setting.getSize(),bitmap,data.setting);
			BitmapMarge marge = data.setting.getMargeBitmap();
			if (marge.getBitmap() != null) {
				Bitmap margeBitmap = bitmapDisplayer. transformBitmap(data.setting.getSize(),marge.getBitmap(),data.setting);
				bitmap = BitmapUtils.combineImagesLeft(margeBitmap,roundBitmap,marge.isSameView());
			}
			else if(marge.getBitmaps() != null) {
				List<Bitmap> bitmaps = marge.getBitmaps();
				if (bitmaps.size() == 0) {
					bitmap = roundBitmap;
				}
				else if (bitmaps.size() == 1) {
					Bitmap margeBitmap = bitmapDisplayer.transformBitmap(data.setting.getSize(),bitmaps.get(0),data.setting);
					bitmap = BitmapUtils.combineImagesLeft(margeBitmap,roundBitmap,marge.isSameView());
				}
				else {
					int count = 0;
					Bitmap leftBitmap = null;
					Bitmap topOneBitmap = null;
					Bitmap leftOneBitmap = null;
					int topCount = 0;
					for (Bitmap b : bitmaps) {
						if ((b == null) || (marge.getMargeLimit() != 0 && count == marge.getMargeLimit()))
							break;
						count +=1;
						Bitmap margeBitmap = bitmapDisplayer.transformBitmap(data.setting.getSize(),b,data.setting);
						if (leftBitmap != null) {
							if (topOneBitmap != null) {
								Bitmap topTwoBitmap = BitmapUtils.combineImagesLeft(topOneBitmap,margeBitmap,marge.isSameView());
								bitmap = BitmapUtils.combineImagesTop(leftBitmap,topTwoBitmap,marge.isSameView());
								putMargeBitmapPixelWithKey(marge.getMargeKey(), getBitmapPixel(topOneBitmap), URL_4);
								putMargeBitmapPixelWithKey(marge.getMargeKey(), getBitmapPixel(margeBitmap), URL_4);
								if (marge.isEnableFullMarge())
									leftBitmap = bitmap;
								topOneBitmap = null;
							} else {
								if (count == bitmaps.size()) {
									bitmap = BitmapUtils.combineImagesTop(leftBitmap,margeBitmap,marge.isSameView());
									putMargeBitmapPixelWithKey(marge.getMargeKey(), getBitmapPixel(margeBitmap), URL_3);
									break;
								} else
									topOneBitmap = margeBitmap;
							}
							topCount +=1;
						} else {
							if (marge.isDisableUriBitmap()) {
								if (leftOneBitmap != null) {
									leftBitmap = BitmapUtils.combineImagesLeft(leftOneBitmap,margeBitmap,marge.isSameView());
									putMargeBitmapPixelWithKey(marge.getMargeKey(), getBitmapPixel(margeBitmap), URL_2);
									putMargeBitmapPixelWithKey(marge.getMargeKey(), getBitmapPixel(leftOneBitmap), URL_2);
									if (count == bitmaps.size()) {
										bitmap = leftBitmap;
										break;
									}
									leftOneBitmap = null;
								} else {
									leftOneBitmap = margeBitmap;
								}
							} else
								leftBitmap = BitmapUtils.combineImagesLeft(roundBitmap,margeBitmap,marge.isSameView());
							putMargeBitmapPixelWithKey(marge.getMargeKey(), getBitmapPixel(roundBitmap), URL_1);
							putMargeBitmapPixelWithKey(marge.getMargeKey(), getBitmapPixel(margeBitmap), URL_1);
						}
					}
				}
			}
		}
		return bitmap;
	}

	public void submitTask(final PhotoTask task) {

        final String url = task.getData().getUriRequest().getUriName();
		if(options.shouldFastOnCreate()) {
			try {
				this.options = new LoaderOptions.OptionsBuilder(options.context)
						.renewWithOld(options).makeLoaderFastOnCreate(false).build();
				loaderSet.time(TimeUtils.SECOND.ONE_SECOND).type(LoaderSet.Type.PAUSE).run();
				pause();
			} catch (final Exception e) {
				log_w("failed to make fast on create");
			}
		}

        boolean isLoadingTaskShutdown = isShutdown();
		if (isLoadingTaskShutdown) {
			checkShutdown();
			task.quitThread();
			log_i(task.toString());
			return;
		}

        if (shouldExecute(task, task.getData().setting, url, true)) {

            taskHunter.execute(new Runnable() {
                @Override public void run() {
                    log_w(SUBMITTING_TASK);

                    if (!singleCallbackEnabled) {
                        if (task.getData().setting.hasCallback()) {
                            String key = task.getCacheKey();
                            LoaderWorker work = worker.get(key);
                            if (work == null) {
                                work = new LoaderWorker(task, key);
                                worker.put(key, work);
                                work.addCallback(task.getData().setting.getCallback().get());
                            } else {
                                work.addCallback(task.getData().setting.getCallback().get());
                            }
                            callback.init(task,LoaderManager.this);
                        }
                    }
                    if (task instanceof AsyncTaskLoader) {
                        executeTask((AsyncTaskLoader) task);
                    }
                    else if (task instanceof SimpleTaskLoader) {
                        executeTask((SimpleTaskLoader) task);
                    }
                    else {
                        if (isCachedInDisc(task.currentLoadingUri()))
                            task.execute(cacheTaskExecutor);
                        else
                            task.execute(loadingTaskExecutor);
                    }
                }});
        }
		checkMemoryTime();
	}

	private boolean shouldExecute(final PhotoTask task, final LoaderSettings setting, String url, boolean fireFailedCallback) {
		if (options.isWaitForUrlBeforeLoading()) {
			if (isSameUrlHasSimpleStreamTask(url) && networkUri(url)) {
				setSimpleStreamWaitingTask(url,task,false);
				return false;
			}
		}

		boolean websiteAccessGranted = websiteAccessIfShouldCheck(setting);
		boolean urlAccessGranted = urlValidIfShouldCheck(setting);
		if (url.equals(AsyncImage.FREEDOM_MARGE_BITMAPS_URI)) {
			//do nothing
		}  else if (!websiteAccessGranted || !urlAccessGranted) {
            if(fireFailedCallback && task.getCallback() != null) {
                task.getCallback().onFailed(new Failed(
                        Failed.FailMethod.URL_INVALID, !websiteAccessGranted ?
                "access denied for the website url by user"
                        : "website url has no valid image extension, disable checkValidUrlBeforeLoading(false)"), null, null);
            }
            return false;
		}
		return true;
	}

	private boolean websiteAccessIfShouldCheck(LoaderSettings setting) {
		if (setting != null) {
			List<String> websites = setting.options.getAllowWebsites();

			if (websites != null) {
				ImageValidator imageValidator = setting.options.getImageValidator();
				ImgValidator validator = imageValidator.websiteMatch(setting.getUriName(),websites);
				if (validator.isNetworkUrl) {
					if (validator.isValid)
						return true;
					else
						return false;
				}
			}
		}
		return true;
	}

	private boolean urlValidIfShouldCheck(LoaderSettings setting) {
		if (setting != null && setting.options.isCheckValidUrlBeforeLoading()) {
			if (cacheKeysForFailedUri.containsKey(setting.getUriName())) {
				return false;
			}
			ImageValidator imageValidator = setting.options.getImageValidator();
			ImgValidator validator = imageValidator.isValidImageUrl(setting.getUriName());
			if (validator.isNetworkUrl) {
				if (validator.isValid) {
					return true;
				} else {
					putCacheKeyForFailedUrl(setting.getUriName(),true);
					return false;
				}
			}
		}
		return true;
	}

	public int getBitmapPixel(Bitmap bitmap) {
		ImageView view = new ImageView(options.context);
		view.setImageBitmap(bitmap);
		return BitmapUtils.getHotspotColor (bitmap, 0, 0);
	}

	public String getMargeBitmapKeyWithPixel(String key, int pixel) {
		if (margeBitmapCachePixel != null && margeBitmapCachePixel.size() != 0) {
			Map<Integer, String> keyPixels = margeBitmapCachePixel.get(key);
			if (keyPixels != null) {
				String isFind =  keyPixels.get(pixel);
				if (isFind != null) {
					return isFind;
				} else {
					int tolerance = 25;
					for (int cPkey : keyPixels.keySet()) {
						if (BitmapUtils.closeBitmapMatch(cPkey, pixel, tolerance)) {
							return keyPixels.get(cPkey);
						}
					}
				}
			} else {
				return null;
			}
		}
		return null;
	}

	public boolean putMargeBitmapPixelWithKey(String key, int pixel, String pixelKey) {
		if (margeBitmapCachePixel != null) {
			Map<Integer, String> keyPixels = margeBitmapCachePixel.get(key);
			if (keyPixels == null) {
				keyPixels = Collections.synchronizedMap(new WeakHashMap<Integer, String>());
			}
			keyPixels.put(pixel,pixelKey);
			margeBitmapCachePixel.put(key,keyPixels);
		}
		return true;
	}

	public boolean removeMargeBitmapPixelWithKey(String key) {
		margeBitmapCachePixel.remove(key);
		return true;
	}

	public boolean putCacheKeyForSimilarSize(String incomingFind,String similarSizeFound) {
		cacheKeysForSimilarSize.put(incomingFind,similarSizeFound);
		return true;
	}

	public boolean removeCacheKeyForSimilarSize(String incomingFind) {
		cacheKeysForSimilarSize.remove(incomingFind);
		return true;
	}

	public String getCacheKeyForSimilarSize(String incomingFind) {
		return cacheKeysForSimilarSize.get(incomingFind);
	}

	public boolean putCacheKeyForFailedUrl(String url,boolean isFailed) {
		cacheKeysForFailedUri.put(url,isFailed);
		return true;
	}

	public boolean removeCacheKeyForFailedUrl(String url) {
		cacheKeysForFailedUri.remove(url);
		return true;
	}

	public LinkedHashSet<PhotoTask> getPendingDownloads() {
		return mPendingDownloads;
	}

	public void addPendingDownload(PhotoTask task) {
		mPendingDownloads.add(task);
	}

	public boolean isSameUrlHasSimpleStreamTask(String url) {
		boolean isLoading = options.isWaitForUrlBeforeLoading() ? mSimpleStreamWaitingTasks.size() != 0 ? mSimpleStreamWaitingTasks.containsKey(url) : false : false;
		return isLoading;
	}

	public boolean setSimpleStreamWaitingTask(String url,PhotoTask task,boolean isNoTask) {
		if (options.isWaitForUrlBeforeLoading()) {
			if (isNoTask) {
				if (!isSameUrlHasSimpleStreamTask(url)) {
					mSimpleStreamWaitingTasks.put(url,null);
					return true;
				}
				return false;
			}
			if (mSimpleStreamWaitingTasks.size() != 0 && isSameUrlHasSimpleStreamTask(url)) {
				List<PhotoTask> tasks = mSimpleStreamWaitingTasks.get(url);
				if (tasks == null)
					tasks = new ArrayList<PhotoTask>();
				if (task != null)
					tasks.add(task);

				mSimpleStreamWaitingTasks.put(url,tasks);
				return true;
			}
		}
		return false;
	}

	public boolean removeSimpleStreamWaitingTask(String url) {
		if (isSameUrlHasSimpleStreamTask(url)) {
			mSimpleStreamWaitingTasks.remove(url);
			return true;
		} else
			return false;
	}

	public boolean reloadSimpleStreamWaitingTask(String url) {
		if (isSameUrlHasSimpleStreamTask(url)) {
			List<PhotoTask> tasks = mSimpleStreamWaitingTasks.get(url);
			mSimpleStreamWaitingTasks.remove(url);

			if (tasks != null) {
				for (PhotoTask task : tasks) {
					if (task != null)
						submitTask(task);
				}
				return true;
			}
		}
		return false;
	}

	public Hashtable<String, ArrayList<AsyncTaskLoader>> getAsyncWaitingTasks() {
		return mAsyncWaitingTasks;
	}

	public void addAsyncWaitingTask(String uri,ArrayList<AsyncTaskLoader> wrappers) {
		mAsyncWaitingTasks.put(uri,wrappers);
	}

	public boolean reloadAsyncWaitingTask(String url) {
		if (isSameUrlHasWaitingTask(url)) {
			List<AsyncTaskLoader> tasks = getAsyncWaitingTasks().get(url);
			getAsyncWaitingTasks().remove(url);

			if (tasks != null) {
				for (AsyncTaskLoader  pending : tasks) {
					executeTask(pending);
					tasks.remove(pending);
				}
				return true;
			}
		}
		return false;
	}

	public boolean isSameUrlHasWaitingTask(String url) {
		boolean isLoading = mAsyncWaitingTasks.size() != 0 ? mAsyncWaitingTasks.containsKey(url) : false;
		return isLoading;
	}

	/** Check if device connection is connected. */
	public boolean hasConnection() {
		boolean connection = options.networkManager.checkConnection();
		log_i(CHECKING_CONNECTION);
		if(connection) {
			log_i(CONNECTION_FOUND);
			return true;
		}
		log_i(CONNECTION_NOT_FOUND);
		return false;
	}

	private void checkMemoryTime() {
		long timeMillis = options.discMemoryCacheOptions.cacheMemoryForMills;
		if(timeMillis != 0) timeExpire(timeMillis);
	}

	public void createExecutor() {
		log_i("Creating Executor: ");
		messagePost(CREATE_EXECUTOR, "loading");
	}

	private Executor createTaskExecutor() {
		return Utility.createExecutor(options.threadPoolSize, options.threadPriority);
	}

	public boolean isCachedInDisc(String uri) {
		if (options.discCache != null) {
			return options.discCache.getFile(uri).exists();
		}
		return false;
	}

	public int uriDelayMillis(String uri) {
		int milis = cacheKeysFordelayUri.get(uri);
		return milis;
	}

	boolean isUriBlock(String uri) {
		return cacheKeysForBlockUri.containsKey(uri);
	}

	boolean isUriDelay(String uri) {
		if(cacheKeysFordelayUri.containsKey(uri))
			return true;
		else
			return false;
	}

	Map<String, String> isUriChange() {
		return cacheKeysForChangeUri;
	}

	public String requestUriView(ImageWrapper wrapper) {
		if (wrapper != null)
			return cacheKeysForView.get(wrapper.getHashCode());
		return null;
	}

	public void addRequestView(ImageWrapper wrapper, String cacheKey) {
		if (wrapper != null )
			cacheKeysForView.put(wrapper.getHashCode(), cacheKey);
	}

	public void cancelRequestView(ImageWrapper wrapper) {
		if (wrapper != null && cacheKeysForView.containsKey(wrapper.getHashCode()))
			cacheKeysForView.remove(wrapper.getHashCode());
	}

	public void cancelRequest(String url) {
		for (PhotoTask task : mPendingDownloads) {
			if (task.currentLoadingUri().equals(url)) {
				task.cancel();
				task.quitThread();
			}
		}
	}

	public void removeBlockUri(String uri) {
		log_i("Lock Uri cacheKey : "+uri+" cancel");
		cacheKeysForBlockUri.remove(uri);
		uriControl.cacheKeysForBlockUri.remove(uri);
	}

	public void clearBlockUri() {
		log_i("Lock uri cacheKey clear: ");
		cacheKeysForBlockUri.clear();
		uriControl.cacheKeysForBlockUri.clear();
	}

	public void removeDelayUri(String uri) {
		log_i("Delay uri cacheKey : "+uri+" remove");
		cacheKeysFordelayUri.remove(uri);
		uriControl.cacheKeysForDelayUri.remove(uri);
	}

	public void clearViewKeyCache() {
		log_i("Uri imageView cacheKey clear: ");
		cacheKeysForView.clear();
	}

	public void checkAndShowProgressView(View iv, boolean visible) {
		if(iv != null){
			if(visible == true){
				if(iv.getVisibility() == View.GONE)
					iv.setVisibility(View.VISIBLE);
			} else if(visible == false){
				if(iv.getVisibility() == View.VISIBLE)
					iv.setVisibility(View.GONE);
			}
		}
	}

	protected void animateBitmap(AnimationStyle animation, DataEmiter emiter, ImageWrapper imageWrapper) {
		PhotoLoadFrom from  = emiter.loadedFrom;
		if ((from == PhotoLoadFrom.NETWORK) || (from == PhotoLoadFrom.LOCAL)
				||  (from == PhotoLoadFrom.DISC)) {

			if (animation != null) {
				Animation anim = null;
				int duration = animation.getDuration();

				if (duration != 0) {
					anim = new AlphaAnimation(0, 1);
					anim.setDuration(duration);
					anim.setInterpolator(new DecelerateInterpolator());
				} else {
					switch (animation.getStyle()) {
						case DEFAULT_FADE_IN:
							anim = new AlphaAnimation(0, 1);
							anim.setDuration(500);
							anim.setInterpolator(new DecelerateInterpolator());
							break;
						case ROTATE:
							anim = BitmapUtils.ranim(600,RotateAnimation.INFINITE,RotateAnimation.RESTART,0,360,50,50,null);
							break;
						case BOUNCE:
							anim = BitmapUtils.sanim(500,1,1,0,1, null);
							anim.setFillAfter(true);
							break;
						case BLINK:;
							anim = BitmapUtils.aanim(600,AlphaAnimation.INFINITE,AlphaAnimation.REVERSE,0,1, null);
							break;
					}
					// end checking animation
				}
				// do work with animation
				if (anim != null &&imageWrapper != null)
					imageWrapper.doAnimation(anim,"start");
			}
		}
	}

	Object mPauseLock() {
		return mPauseLock;
	}

	Object mLock() {
		return mLock;
	}

	Object mDelayLock() {
		return mUriDelayLock;
	}

	Object mChangeLock() {
		return mUriChangeLock;
	}

	/** Checks whether ImageWrapper is Cancelled.*/
	boolean isCancelled(ImageWrapper imageWrapper) {
		String imageWrapperCache = requestUriView(imageWrapper);
		return imageWrapperCache == null;
	}

	/** check if loading task is pause. */
	boolean isPause() {
		return mPauseWork;
	}

	/** Get paused time if set. */
	int getPausedTime() {
		return mPauseWorkTime;
	}

	/** check if loading task is shutdown.*/
	boolean isShutdown() {
		return shutdown;
	}

	/** check if loading task is shutdown 
	 *
	 * @throws IllegalStateException if task is shutdown 
	 */
	void checkShutdown() {
		if(shutdown)
			throw new IllegalStateException("Image loader is shut down using shutdown() need to call method start()" +
					"to start loading task again.");
	}

	/** Resumes loader work. and set mPauseWork false
	 * @return resume
	 */
	boolean resume() {
		if(isPausedTime && mPauseWorkTime != 0) return false;
		setPauseWork(false,0);
		return true;
	}

	/**
	 * Pause loader All loading thread set pauses and.<br/>
	 * all running tasks.
	 */
	boolean pause() {
		pause(0);
		return mPauseWork;
	}

	/**
	 * Pause All loading thread for specified time.<br/>
	 * all running tasks.
	 */
	boolean pause(int time) {
		setPauseWork(true,time);
		return mPauseWork;
	}

	boolean isPausedTime(boolean enablePauseTime) {
		if(enablePauseTime == false) mPauseWorkTime = 0;
		return isPausedTime = enablePauseTime;
	}

	void start() {
		log_i("task start");
		shutdown = false;
		messagePost(CREATE_EXECUTOR, "task start");
	}

	boolean cancel() {
		return taskHunter != null
				&& (cacheTaskExecutor != null)
				&& loadingTaskExecutor != null
				&& options == null
				&& this == null;
	}

	public void shutdown() {
		for (ViewTargetWrapper requestTargetWrapper : requestTargetCache.values()) {
			requestTargetWrapper.stopRequest();
		}
		requestTargetCache.clear();
		messagePost(TASK_SHUTDOWN, null);
	}

	public void cancelRequest(View imageView) {
		ViewTargetWrapper requestTargetWrapper =  requestTargetCache.remove(imageView);
		if (requestTargetWrapper != null) {
			requestTargetWrapper.stopRequest();
		}
	}

	boolean shouldUseNoNetwork() {
		return useNoNetwork;
	}

	boolean shouldUseOnlyNetwork() {
		return useOnlyNetwork;
	}

	LoaderManager neverUseNetwork(boolean denyNetwork) {
		useNoNetwork = denyNetwork;
		return this;
	}

	LoaderManager useOnlyNetwork(boolean allowOnlyNetwork) {
		useOnlyNetwork = allowOnlyNetwork;
		return this;
	}

	Executor getHunterTask() {
		return taskHunter;
	}

	Executor getCacheTask() {
		return cacheTaskExecutor;
	}

	Executor getLoadingTask() {
		return loadingTaskExecutor;
	}

	public Map<String, LoaderWorker> getMultiWorker() {
		return worker;
	}

	public WorkerCallback getWorkerCallback() {
		return callback;
	}

	/**
	 * Pause any loading tasks. This can be used to improve ListView or GridView performance.
	 * when scrolling using a
	 *
	 * call setPauseWork(false) to resume task
	 * call setPauseWork(true) to pause task
	 */
	public void setPauseWork(boolean pauseWork,int time) {
		synchronized (mPauseLock) {
			mPauseWork = pauseWork;
			if (!mPauseWork) {
				mPauseLock.notifyAll();
			}
			mPauseWorkTime = time;
			mPauseWork = pauseWork;
		}
	}

	/**
	 * Lock URLEncoded using { ava.util.concurrent.locks.ReentrantLock ReentrantLock}.
	 * when loading
	 *
	 * @return lockUri
	 */
	public ReentrantLock putLockUri(String uri) {
		ReentrantLock lockUri = cacheLocksForUri.get(uri);
		if (lockUri == null)
			cacheLocksForUri.put(uri, lockUri = new ReentrantLock());
		return lockUri;
	}

	/**
	 * Stop any loading animation if required
	 */
	void stopAnimation(ImageWrapper imageWrapper) {
		if(imageWrapper.getView() != null) {
			imageWrapper.doAnimation(null,"");
		}
	}

	/** Cancel task*/
	void cancelTask(RequestData data) {
		messagePost(TASK_CANCELLED, data);
	}

	void putTargetRequest(View view, ViewTargetWrapper request) {
		requestTargetCache.put(view, request);
	}

	private void checkTimeExpire(final long time) {
		if(time != 0) {
			loaderSet.time((int)time).type(LoaderSet.Type.CLEAR_MEMORY).run();
		}
	}

	private void messagePost(int type,Object object) {
		switch (type) {
			case TASK_SHUTDOWN:
				if (loggingEnabled) log_i("task stop: ");
				shutdown = true;
				shutdownExecutors(); clearMemoryCache();
				break;
			case TASK_DESTORY:
				if (loggingEnabled) log_i("task Destory: ");
				mPauseWork = false;
				shutdown();
				break;
			case TASK_SUBMIT:
				if (loggingEnabled) log_i(TASK_SUBMITED);
				executeTask((SimpleTaskLoader) object);
				break;
			case CREATE_EXECUTOR:
				if (loggingEnabled) log_i("taskExecutor create: ");
				createExecutorService();
				break;
			case CACHE_TIME_CLEAR:
				if (loggingEnabled) log_i("Task Cache time clear: ");
				checkTimeExpire((Long) object);
				break;
			case TASK_CANCELLED:
				if (loggingEnabled) log_i("Task cancelled: ");
				break;
			case NETWORK_FAILED:
				if (loggingEnabled) log_i("Network failed: ");
				break;
			default:
				if (loggingEnabled)
					log_w("Handler cant Recognize: ");
				break;
		}
	}

	/**
	 * create bitmap options  {@link android.graphics.BitmapFactory.Options}
	 *
	 * @param data Needed data to get bitmap options
	 * @return {@link android.graphics.BitmapFactory.Options} BitmapFactory.Options
	 */
	public BitmapFactory.Options createBitmapOptions(RequestData data) {
		final boolean hasConfig = data.setting.bitmapConfig != null;
		final boolean hasOptions = data.setting.decodingOptions != null;
		BitmapFactory.Options options = null;
		if(hasOptions) {
			options = data.setting.decodingOptions;
		} else if (hasConfig) {
			options = new BitmapFactory.Options();
			options.inPreferredConfig = data.setting.bitmapConfig;
		}
		return options;
	}

	/**
	 * Check loading URLEncoded
	 *
	 * @param uri check if URLEncoded is to be load from network
	 * Returns <tt>true</tt> - if image should be loaded from network
	 */
	public boolean networkUri(String uri) {
		switch (UriScheme.match(uri)) {
			case HTTP: case HTTPS:
				return true;
			default:
				return false;
		}
	}

	private void createExecutorService() {
		if (loggingEnabled)  log_i("taskExecutor create: ");
		taskHunter = Executors.newCachedThreadPool();
		loadingTaskExecutor = createTaskExecutor();
		cacheTaskExecutor = createTaskExecutor();
	}

	private void shutdownExecutors() {
		if (loadingTaskExecutor != null)
			((ExecutorService) loadingTaskExecutor).shutdownNow();
		if (taskHunter != null)
			((ExecutorService) taskHunter).shutdownNow();
		if (cacheTaskExecutor != null)
			((ExecutorService) cacheTaskExecutor).shutdownNow();
	}

	private void checkAndInitExecutors() {
		if (!options.initExecutor && isLoadingExecutorShutdown()) {
			loadingTaskExecutor = createTaskExecutor();
		}
		if (!options.initExecutor && isCacheExecutorShutdown()) {
			cacheTaskExecutor = createTaskExecutor();
		}
	}

	private void executeTask(SimpleTaskLoader phototask) {
		checkAndInitExecutors();
		if (isCachedInDisc(phototask.currentLoadingUri())) {
			cacheTaskExecutor.execute(phototask);
		} else {
			loadingTaskExecutor.execute(phototask);
		}
	}

	private void executeTask(AsyncTaskLoader asynctask) {
		checkAndInitExecutors();
		if (isCachedInDisc(asynctask.uri)) {
			asynctask.execute(cacheTaskExecutor);
		} else {
			asynctask.execute(loadingTaskExecutor);
		}
	}

	/**
	 * check if Cache Executor has been shutdown. 
	 *
	 * Returns <tt>true</tt> - if cache executor is shutdown
	 */
	public boolean isCacheExecutorShutdown() {
		if (((ExecutorService) cacheTaskExecutor).isShutdown()) {
			return true;
		}
		return false;
	}

	/**
	 * check if Loading Executor has been shutdown. 
	 *
	 * Returns <tt>true</tt> - if loading executor is shutdown
	 */
	public boolean isLoadingExecutorShutdown() {
		if (((ExecutorService) loadingTaskExecutor).isShutdown()) {
			return true;
		}
		return false;
	}

	/**
	 * delete file from disc cache.
	 *
	 * @param file file to be deleted
	 * Returns <tt>true</tt> - if file is deleted
	 */
	public boolean delete(File file) {
		if(file.exists()) {
			return file.delete();
		}
		return true;
	}

	/**
	 * check if file exists but failed to load.
	 *
	 * Returns <tt>true</tt> - if file is failed
	 */
	boolean checkFileFailed(File file) {
		if(file.exists()) {
			if(file == null || file.length() == 0) if(file.exists()) file.delete();
			return file == null || file.length() == 0;
		}
		return file == null || file.length() == 0;
	}

	/**
	 * time for memory cache to be clears.
	 *
	 * @param time clear memory cache after time exceeds
	 */
	void timeExpire(long time) {
		if(time != 0) messagePost(CACHE_TIME_CLEAR, time);
	}

	/**
	 * clear all bitmap from memory cache.
	 */
	void clearMemoryCache() {
		options.memoryCache.clear();
	}

	/**
	 * Puts bitmap into memory cache by key.
	 *
	 * @param bitmap cache {@code Bitmap} for {@code key}
	 * @param key the cache key for {@code Bitmap}
	 */
	void putToMemory(String key,Bitmap bitmap) {
		options.memoryCache.put(key, bitmap);
	}

	/**
	 * Check if bitmap is in memory cache by {@code key}.
	 *
	 * @param cacheKey cache {@code key} for bitmap
	 * @return bitmap
	 */
	Bitmap isBitmapInMemoryCache(String cacheKey) {
		return options.memoryCache.get(cacheKey);
	}

	/**
	 * Check if the bitmap is valid.
	 *
	 * @param bitmap the bitmap to check
	 * @return true if bitmap is OK
	 */
	boolean isValidBitmap(Bitmap bitmap) {
		return bitmap != null;
	}

	/**
	 * update file last modified with currentTimeMillis. 
	 *
	 * @param file file to update
	 * Returns <tt>true</tt> - if file is update with currentTimeMillis
	 */
	boolean updateLastModifiedForCache(File file){
		long now = System.currentTimeMillis();
		return file.setLastModified(now);
	}

	/**
	 * check if specific cache time is expire. 
	 *
	 * Returns <tt>true</tt> - if time is expire
	 */
	boolean isCacheTimeExpire(File file, long time) {
		return time == Integer.MAX_VALUE || System.currentTimeMillis() < file.lastModified() + time;
	}

	private void log_i(String message) {
		if (loggingEnabled) Logg.info(message);
	}

	private void log_w(String message) {
		if (loggingEnabled) Logg.warning(message);
	}

	public class LoaderWorker {
		private final PhotoTask task;
		private final String key;
		private final List<LoaderCallback> cbs = new ArrayList<LoaderCallback>();

		public LoaderWorker(PhotoTask task, String key) {
			this.task = task;
			this.key = key;
		}

		public void addCallback(LoaderCallback cb) {
			cbs.add(cb);
		}

		public void remove(LoaderCallback cb) {
			cbs.remove(cb);
			if (cbs.size() == 0) {
				task.quitThread();
				worker.remove(key);
			}
		}

		public boolean loaded(final DataEmiter dataEmiter,final ImageView view) {
			if(!cbEnabled()) return false;
			for (LoaderCallback cb : cbs) {
				cb.onSuccess(dataEmiter, view);
			}
			return true;
		}

		public boolean failed(final Failed failed,String uri,final ImageView view) {
			if(!cbEnabled()) return false;
			return run(CallType.FAILED,failed,uri, view);
		}

		public boolean progress(LoaderSettings setting,final ProgressCallback pcb,final int bytes,final int total) {
			if(pcb != null) {
				Runnable rCallback = new Runnable() {
					@Override public void run() {
						pcb.onProgressUpdate(bytes, total);
					}};
				setting.getHandler().post(rCallback);
			}
			return true;
		}

		boolean run(final CallType type,final Object object,final String uri,final ImageView view) {
			Runnable rCallback = new Runnable() {
				@Override public void run() {
					switch (type) {
						case LOADED:
							for (LoaderCallback cb : cbs) {
								cb.onSuccess((DataEmiter) object, view);
							}
							worker.remove(key);
							break;
						case FAILED:
							for (LoaderCallback cb : cbs) {
								cb.onFailed((Failed) object, uri,view);
							}
							worker.remove(key);
							break;
						default:
							break;
					}
				}
			};
			task.getData().setting.getHandler().post(rCallback);
			return true;
		}

		public boolean cbEnabled() {
			return cbs != null && !cbs.isEmpty() && cbs.size() != 0;
		}
	}
}
