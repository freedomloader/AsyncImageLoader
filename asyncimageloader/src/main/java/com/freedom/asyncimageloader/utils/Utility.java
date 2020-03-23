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
package com.freedom.asyncimageloader.utils;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.HONEYCOMB_MR1;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;

import com.freedom.asyncimageloader.assist.BitmapDecoder;
import com.freedom.asyncimageloader.assist.ImageStreamLoader;
import com.freedom.asyncimageloader.cache.DiscCache;
import com.freedom.asyncimageloader.cache.MemoryCache;
import com.freedom.asyncimageloader.disc.LimitedDiscCache;
import com.freedom.asyncimageloader.disc.LruDiscCache;
import com.freedom.asyncimageloader.download.Downloader;
import com.freedom.asyncimageloader.download.ImageDownloader;
import com.freedom.asyncimageloader.download.LoadStream;
import com.freedom.asyncimageloader.exception.FailedReadingException;
import com.freedom.asyncimageloader.listener.CusListener;
import com.freedom.asyncimageloader.memory.LimitedMemoryCache;
import com.freedom.asyncimageloader.memory.LruMemoryCache;
import com.freedom.asyncimageloader.network.NetworkManager;
import com.freedom.asyncimageloader.network.NetworkService;

public class Utility {

    static final String ANDROID_STORAGE_ROOT = "/Android/data/";
    static final String DEFAULT_IMAGE_FOLDER = "/cache/images";
    
	public static int DEFAULT_DECODE_FILE_QUALITY = 100;
	private static final int IO_BUFFER_SIZE = 32 * 1024;

	public static boolean CopyStream(InputStream input, OutputStream output)
			throws IOException {
		return CopyStream(input,output,null);
	}

	public static boolean CopyStream(InputStream input, OutputStream output,
			CusListener.BytesListener listener) throws IOException {
		int current = 0;
		final int lenghtOfFile = input.available();

		final byte[] bytes = new byte[IO_BUFFER_SIZE];
		int count;
		try {
			checkLoading(listener, current, lenghtOfFile);
			while ((count = input.read(bytes, 0, IO_BUFFER_SIZE)) != -1) {
				output.write(bytes, 0, count);
				current += count;
				checkLoading(listener, current, lenghtOfFile);
			}
		} catch (FailedReadingException e) {
			return false;
		}
		return true;
	}

	public InputStream fileStream(File from) throws IOException {
		InputStream in = null;
		try {
			in = new FileInputStream(from);
		} catch (IOException e) {
			throw e;
		} finally {
			closeQuietly(in);
		}
		return in;
	}

	public static int getBitmapBytes(Bitmap bitmap) {
		int resultByte;
		if (SDK_INT >= HONEYCOMB_MR1) {
			resultByte = BitmapHoneycombMR1.getByte(bitmap);
		} else {
			resultByte = bitmap.getRowBytes() * bitmap.getHeight();
		}
		if (resultByte < 0) {
			throw new IllegalStateException("error bitmap bytes: " + bitmap);
		}
		return resultByte;
	}

	public static void closeQuietly(Closeable c) {
		try {
			if (c != null) {
				c.close();
			}
		} catch (Exception e) {
			// do nothing
		}
	}

	/**
	 * Reads all data from stream and close it silently
	 * 
	 * @param is
	 *            Input stream
	 */
	public static void closeStream(InputStream is) {
		final byte[] bytes = new byte[IO_BUFFER_SIZE];
		try {
			while (is.read(bytes, 0, IO_BUFFER_SIZE) != -1) {
			}
		} catch (IOException e) {
		} finally {
			closeQuietly(is);
		}
	}
	
	/**
	 * @param bytesListener  copying bytes to listener
	 * @param current  loaded bytes
	 * @param lenghtOfFile  max size of file
	 * 
	 * @throws FailedReadingException if should interrupt by listener
	 */
	private static boolean checkLoading(CusListener.BytesListener bytesListener,
			int current, int lenghtOfFile) throws FailedReadingException {
		if (bytesListener != null) {
			//check if progress callback is not null if is null return false
			//check if task interrupted or view not active if true then throw FailedReadingException()
			if (!bytesListener.onBytesUpdate(current, lenghtOfFile)) {
				//check if current bytes match 75% do nothing
				if (100 * current / lenghtOfFile < 75) {
					//throw Exception if interrupt
					throw new FailedReadingException();
				}
			}
		}
		//successful copying
		return false;
	}
	
	public static boolean isExternalStorageRemovable() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			return Environment.isExternalStorageRemovable();
		}
		return true;
	}

	/*public static MemoryCache createMemoryCache() {
		return 
	}*/

	public static MemoryCache createMemoryCache(int maxMemoryCacheSize) {
		if (maxMemoryCacheSize == 0) {
			maxMemoryCacheSize = ( int ) (Runtime. getRuntime().maxMemory() / 8);
		}
		return new LruMemoryCache(maxMemoryCacheSize);
	}

	public static MemoryCache createLimitedMemoryCache(int maxMemoryCacheSize) {
		if (maxMemoryCacheSize == 0) {
			final int maxMemory = ( int ) (Runtime. getRuntime().maxMemory() / 1024);
			maxMemoryCacheSize = maxMemory / 8;
		}	   
		return new LimitedMemoryCache(maxMemoryCacheSize);
	}
	  
	public static Downloader createImageDownloader(Context context,ImageStreamLoader streamLoader) {
		return new ImageDownloader(context, streamLoader);
	}

	public static DiscCache createDiskCache(Context context) {
		return new LruDiscCache(context);
	}

	public static DiscCache createSizeDiskCache(Context context,
			int maxDiscCacheSize) {
		return new LimitedDiscCache(context, maxDiscCacheSize);
	}

	public static Handler createHandler() {
		return new Handler();
	}
	
	public static ImageStreamLoader createImageStream(Context context,NetworkManager network) {
		return new LoadStream(context,network);
	}

	public static FileNameGenerator createFileNameGenerator() {
		return new FileKeyGenerator.HASH();
	}

	public static BitmapDecoder createBitmapDecoder() {
		return new BitmapDecoder();
	}

	public static NetworkManager createNetworkService(Context context) {
		return new NetworkService(context);
	}

	public static Executor createExecutor(int threadPoolSize,
			int threadPriority, long keepAlive, TimeUnit timeUnit,
			BlockingQueue<Runnable> blockingQueue) {
		return new ThreadPoolExecutor(threadPoolSize, threadPoolSize,
				keepAlive, timeUnit, blockingQueue, new LoaderThreadFactory(
						threadPriority));
	}

	public static Executor createExecutor(int threadPoolSize, int threadPriority) {
		BlockingQueue<Runnable> blockingQueue = new LinkedBlockingQueue<Runnable>();
		return new ThreadPoolExecutor(threadPoolSize, threadPoolSize, 0L,
				TimeUnit.MILLISECONDS, blockingQueue, new LoaderThreadFactory(
						threadPriority));
	}

	private static class LoaderThreadFactory implements ThreadFactory {
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		private static final AtomicInteger socNum = new AtomicInteger(1);

		private final int priority;
		private final ThreadGroup threadgroup;
		private final String frredomThreadName;
		private static final int STACK_SIZE = 0;

		LoaderThreadFactory(int priority) {
			this.priority = priority;
			SecurityManager security = System.getSecurityManager();
			threadgroup = (security != null) ? security.getThreadGroup()
					: Thread.currentThread().getThreadGroup(); 
			frredomThreadName = "freedom-loader-" + 
					socNum.getAndIncrement() + "-thread-";
		}

		@Override
		public Thread newThread(Runnable runnable) {
			Thread thread = new Thread(threadgroup, runnable, frredomThreadName
					+ threadNumber.getAndIncrement(), STACK_SIZE);
			if (thread.isDaemon())
				thread.setDaemon(false);
			thread.setPriority(priority);
			return thread;
		}
	}

	public static byte[] toBytes(InputStream is) {
		byte[] result = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {
			CopyStream(is, baos);
			result = baos.toByteArray();
		} catch (IOException e) {
			Logg.debug(e.getMessage());
		}
		closeQuietly(is);
		return result;
	}
	   
	/* Checks if external storage is available for read and write */
	public static boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	/* Checks if external storage is available to at least read */
	public static boolean isExternalStorageReadable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)
				|| Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return true;
		}
		return false;
	}
	public static String updateLoadingUsage(float startTime, Bitmap bitmap) {
		// Calculate memory usage and performance statistics
		final int memUsageKb = (bitmap.getRowBytes() * bitmap.getHeight()) / 1024;
		final long stopTime = SystemClock.uptimeMillis();
		return "Loading Time: " + (stopTime - startTime)
				+ " ms. Memory used for decoding and scale bitmap: "
				+ memUsageKb + " kb.";
	}

	public class FlushedInputStream extends FilterInputStream {

		public FlushedInputStream(InputStream inputStream) {
			super(inputStream);
		}

		@Override
		public long skip(long n) throws IOException {
			long totalBytesSkipped = 0L;
			while (totalBytesSkipped < n) {
				long bytesSkipped = in.skip(n - totalBytesSkipped);
				if (bytesSkipped == 0L) {
					int by_te = read();
					if (by_te < 0) {
						break; // we reached EOF
					} else {
						bytesSkipped = 1; // we read one byte
					}
				}
				totalBytesSkipped += bytesSkipped;
			}
			return totalBytesSkipped;
		}
	}
	
	/**
	 * Unzip a zip file. Will overwrite existing files.
	 * 
	 * @param zipFile Full path of the zip file you'd like to unzip.
	 * @param location Full path of the directory you'd like to unzip to (will be
	 *            created if it doesn't exist).
	 * @throws IOException
	 */
	public static void unzip(String zipFile, String location) throws IOException {
		int size;
		byte[] buffer = new byte[IO_BUFFER_SIZE];
		try {
			if (!location.endsWith("/")) {
				location += "/";
			}
			File f = new File(location);
			if (!f.isDirectory()) f.mkdirs();
			ZipInputStream zin = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile), IO_BUFFER_SIZE));
			try {
				ZipEntry ze = null;
				while ((ze = zin.getNextEntry()) != null) {
					String path = location + ze.getName();
					File unzipFile = new File(path);
					if (ze.isDirectory()) {
						if (!unzipFile.isDirectory()) unzipFile.mkdirs();
					} else {
						File parentDir = unzipFile.getParentFile();
						if (null != parentDir) {
							if (!parentDir.isDirectory()) parentDir.mkdirs();
						}
						FileOutputStream out = new FileOutputStream(unzipFile, false);
						BufferedOutputStream fout = new BufferedOutputStream(out, IO_BUFFER_SIZE);
						try {
							while ((size = zin.read(buffer, 0, IO_BUFFER_SIZE)) != -1) {
								fout.write(buffer, 0, size);
							}
							zin.closeEntry();
						} finally {
							fout.flush();
							fout.close();
						}
					}
				}
			} finally {
				zin.close();
			}
		} catch (Exception e) {
			Logg.error(e, "Unzip exception");
		}
	}

	public static boolean unpackZip(String path, String zipname) {
		InputStream is;
		ZipInputStream zis;
		try {
			is = new FileInputStream(path + zipname);
			zis = new ZipInputStream(new BufferedInputStream(is));
			ZipEntry ze;
			while ((ze = zis.getNextEntry()) != null) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] buffer = new byte[1024];
				int count;
				String filename = ze.getName();
				FileOutputStream fout = new FileOutputStream(path + filename);
				// reading and writing
				while ((count = zis.read(buffer)) != -1) {
					baos.write(buffer, 0, count);
					byte[] bytes = baos.toByteArray();
					fout.write(bytes);
					baos.reset();
				}
				fout.close();
				zis.closeEntry();
			}
			zis.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

    public static File createDefaultDiscCacheDirectory(Context context) {
        File cacheDir = null;
        if (isMounted()) {
    	    cacheDir = createImageCacheDir(context);
        } else {
    	    cacheDir = context.getCacheDir();
        }
	    try {
			new File(cacheDir, ".nomedia").createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         return cacheDir;
     }
 
    private static File createImageCacheDir(Context context) {
       File cacheDir = null;
	   String cachePath = ANDROID_STORAGE_ROOT + context.getPackageName() + DEFAULT_IMAGE_FOLDER;
	   cacheDir = new File(getExternalStorageDirectory(), cachePath);
       if (!cacheDir.isDirectory()) {
     	  cacheDir.mkdirs();
       }
       return cacheDir;
     }
     
	protected static boolean isMounted() {
	    if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
	        return true;
	    }
	    return false;
	}
	
	protected static File getExternalStorageDirectory() {
        return android.os.Environment.getExternalStorageDirectory();
    }
	
	public static String getExtension(String path, boolean removeDot) {
		return getExtension(new File(path), removeDot);
	}

	/*
	 * Get the extension of a file.
	 */
	public static String getExtension(File f, boolean removeDot) {
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');
		if (i > 0 && i < s.length() - 1) {
			if (removeDot) {
				ext = s.substring(i + 1).toLowerCase();
			} else {
				ext = s.substring(i).toLowerCase();
			}
		}
		return ext;
	}

	@TargetApi(HONEYCOMB_MR1)
	private static class BitmapHoneycombMR1 {
		static int getByte(Bitmap bitmap) {
			return bitmap.getByteCount();
		}
	}
}
