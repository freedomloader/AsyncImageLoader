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
package com.freedom.asyncimageloader.assist;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Reference;

import com.freedom.asyncimageloader.DiscMemoryCacheOptions;
import com.freedom.asyncimageloader.ImageOptions;
import com.freedom.asyncimageloader.RequestData;
import com.freedom.asyncimageloader.download.Downloader;
import com.freedom.asyncimageloader.uri.UriRequest;
import com.freedom.asyncimageloader.uri.URLEncoded.UriScheme;
import com.freedom.asyncimageloader.utils.BitmapUtils;
import com.freedom.asyncimageloader.utils.Logg;
import com.freedom.asyncimageloader.utils.Utility;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.BitmapFactory.Options;
import android.widget.ImageView;
import android.widget.*;
import com.freedom.asyncimageloader.*;

public class BitmapDecoder implements Decoder{

	public BitmapDecoder(){}
	protected static Reference<ImageView> imgView;

	/**
	 * Decoder function for decoding bitmap.
	 *
	 * @param file file to decode
	 * @return Decoded Bitmap
	 */
	public static Bitmap decodeFile(File file){
		try {
			Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file), null, null);
			return bitmap;
		} catch (FileNotFoundException e) {}
		return null;
	}

	/**
	 * Decoder function for rotating bitmap.
	 *
	 * @param bitmap The bitmap to rotate
	 * @param rotate the specific rotate degrees
	 * @return Rotated Bitmap
	 */
	@Override
	public Bitmap rotate(Bitmap bitmap,int rotate)  throws IOException {
		Bitmap rotatedBitmap = null;
		try {
			// create a matrix object
			Matrix matrix = new Matrix();
			matrix.postRotate(rotate);
			// anti-clockwise by 90 degrees
			// create a new bitmap from the original using 
			// the matrix to transform the result
			rotatedBitmap = Bitmap.createBitmap(bitmap , 0 , 0 , bitmap.getWidth(),
					bitmap.getHeight(), matrix, true );
			// return the rotated bitmap
		} catch (Exception e) {
			Logg.warning("rotating bitmap failed with error: "+e.getMessage());
		}
		return rotatedBitmap;
	}

	/**
	 * Decoder function for decoding an image resource. The decoded bitmap will
	 * be optimized for further scaling to the requested destination dimensions
	 * and scaling logic.
	 *
	 * @param res The resources object containing the image data
	 * @param resId The resource id of the image data
	 * @param dstWidth Width of destination area
	 * @param dstHeight Height of destination area
	 * @return Decoded bitmap
	 */
	public static Bitmap decodeResource(Resources res, int resId, int dstWidth, int dstHeight,
										ImageScale imageScale) {
		Options options = new Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);
		options.inJustDecodeBounds = false;
		options.inSampleSize = calculateSampleSize(options.outWidth, options.outHeight, dstWidth,
				dstHeight, imageScale);
		Bitmap unscaledBitmap = BitmapFactory.decodeResource(res, resId, options);

		return unscaledBitmap;
	}

	public Bitmap decodeFile(RequestForDecode request) throws IOException {
		InputStream cacheImageStream = downloadStream(request.data.setting.options.getDownloader(),
				request.useDownloaderForInputStream,
				request.imageKey,request.data.getUriRequest());
		return decodeFile(request,cacheImageStream);
	}

	/**
	 * Decoder function for decoding an file image. The decoded bitmap will
	 * be optimized for further scaling to the requested size and scaleType
	 * decodes image and scales it to reduce memory consumption.
	 *
	 * @param request The request data for decoding image
	 * @return Decoded bitmap
	 * @throws IOException     if some exception occurs during image decoding
	 */
	public Bitmap decodeFile(final RequestForDecode request,InputStream cacheImageStream) throws IOException {
		Bitmap decodedStreamBitmap;
		Resizer targetSize = request.targetSize;
		LoaderSettings setting = request.data.getSetting();
		//First we have to decode bitmap with inJustDecodeBounds=true to check dimensions
		BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
		bitmapOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(cacheImageStream, null, bitmapOptions);

		cacheImageStream = resetLoadedStream(cacheImageStream, request);
		boolean shouldDecode = targetSize.width != 0 && targetSize.height != 0;

		//Get decoded bitmap size width height
		final Resizer optionSize = new Resizer(bitmapOptions.outWidth,bitmapOptions.outHeight);
		if (shouldDecode == false) {
			targetSize = optionSize;
			shouldDecode = targetSize.width != 0 && targetSize.height != 0;
		}
		if (shouldDecode) {
			targetSize = setting.getTargetSize(targetSize);
		}
		targetSize.width = setting.getMaxWidth(targetSize.width, bitmapOptions.outWidth);
		targetSize.height = setting.getMaxWidth(targetSize.height, bitmapOptions.outHeight);
		request.targetSize = targetSize;

		//decode image options and scales it to reduce memory consumption
		//Find the correct scale value. It should be the power of 2.
		Options options = populateFileDecodingOptions(cacheImageStream,optionSize,request.options,
				targetSize ,request.scaleType);

		//decode input stream into bitmap
		decodedStreamBitmap = decodeInputStream(cacheImageStream, options);
		if (request.updateLastModified && request.updateLastModified) {
			//update file last modified
			//updateLastModifiedForCache(request.getFile());
			//Logg.error(null, "decoding file uri: "+request.imageKey);
		}
		if (decodedStreamBitmap != null) {
			Logg.debug("decoded uri: "+request.imageKey);
			if(request.data != null && shouldDecode && request.canDecodeBitmap) {
				decodedStreamBitmap = decodeBitmap(request,decodedStreamBitmap);
			}
		} else {
			Logg.error(null, "decoding failed uri: "+request.imageKey);
		}
		return decodedStreamBitmap;
	}

	private Options populateFileDecodingOptions(InputStream cacheImageStream,Resizer optionsize,
												Options options,Resizer targetSize,ImageScale scaleType) {
		Options decodeOptions = new Options();
		boolean hasOwnDefaultBitmapOptions = options != null;

		if(hasOwnDefaultBitmapOptions) {
			decodeOptions = options;
		}
		if(decodeOptions.inSampleSize == 0) {
			//Find the correct scale value. It should be the power of 2.
			int scale= calculateSampleScale(optionsize,targetSize,scaleType);
			//decode with inSampleSize
			decodeOptions.inSampleSize=scale;
		}
		return decodeOptions;
	}

	/**
	 * download file input stream.
	 *
	 * @throws IOException     if errors occurs during downloading stream
	 */
	private InputStream downloadStream(Downloader downloader,boolean useDownloaderForInputStream,
									   String imageKey,UriRequest request) throws IOException {
		if(useDownloaderForInputStream) {
			//stream can also be download from
			return downloader.downloadStream(request,null);
		} else {
			String key = UriScheme.FILE.remove(imageKey);
			return new FileInputStream(key);
		}
	}

	/**
	 * decode an input stream as a bitmap using BitmapFactory.decodeStream.
	 * <p/>
	 * If decoding fails the input stream is closed.
	 *
	 * @return bitmap created from the given input stream.
	 * @throws IOException     if errors occurs during BitmapFactory.decodeStream
	 */
	public Bitmap decodeInputStream(InputStream cacheImageStream,Options options) throws IOException {
		Bitmap bitmap = null;
		try {
			bitmap = BitmapFactory.decodeStream(cacheImageStream, null, options);
		} catch (final Throwable e) {
			// calling gc does not help as is called anyway
			// http://code.google.com/p/android/issues/detail?id=8488#c80
			// System.gc();
		} finally {
			Utility.closeQuietly(cacheImageStream);
		}
		return bitmap;
	}

	/**
	 * update file last modified.
	 *
	 * @param f file to update
	 * @return true if is success update.
	 */
	boolean updateLastModifiedForCache(File f) {
		return f.setLastModified(System.currentTimeMillis());
	}

	/**
	 * Calculate optimal down-sampling factor given the dimensions of a source
	 *
	 * @param bmpSize of source image
	 * @param targetSize Height & Width of source image
	 * @param scaleType scaleType of resize target
	 * @return scale
	 */
	public static int calculateSampleScale(Resizer bmpSize,Resizer targetSize,ImageScale scaleType) {
		int srcWidth = bmpSize.getWidth();
		int srcHeight = bmpSize.getHeight();
		int targetWidth = targetSize.getWidth();
		int targetHeight = targetSize.getHeight();
		boolean sureScale = true;
		int scale = 1;

		int scaleWidth = srcWidth / targetWidth;
		int scaleHeight = srcHeight / targetHeight;

		switch (scaleType) {
			case FIT_NORMAL:
				if (sureScale) {
					while (srcWidth / 2 >= targetWidth || srcHeight / 2 >= targetHeight) {
						srcWidth /= 2;
						srcHeight /= 2;
						scale *= 2;
					}
				} else {
					scale = Math.max(scaleWidth, scaleHeight);
				}
				break;
			case CROP:
				while(true){
					if(srcWidth/2<targetWidth || srcHeight/2<targetHeight)
						break;
					srcWidth/=2;
					srcHeight/=2;
					scale*=2;
				}
				break;
			case CENTER:
				if (sureScale) {
					while (srcWidth / 2 >= targetWidth && srcHeight / 2 >= targetHeight) {
						srcWidth /= 2;
						srcHeight /= 2;
						scale *= 2;
					}
				} else {
					scale = Math.min(scaleWidth, scaleHeight);
				}
				break;
			case FIT_XY:
				while(true){
					if(srcWidth/2<targetWidth || srcHeight/2<targetHeight)
						break;
					srcWidth/=2;
					srcHeight/=2;
					scale*=2;
				}
				break;
			default:
				break;
		}
		return scale;
	}


	/**
	 * reset stream.
	 *
	 * @param cacheImageStream input stream to reset
	 * @return cacheImageStream.
	 * @throws IOException     if errors occurs
	 */
	protected InputStream resetLoadedStream(InputStream cacheImageStream, RequestForDecode request) throws IOException {
		try {
			cacheImageStream.reset();
		} catch (IOException e) {
			Utility.closeQuietly(cacheImageStream);
			cacheImageStream = downloadStream(request.data.setting.options.getDownloader(),
					request.useDownloaderForInputStream,
					request.imageKey,request.data.getUriRequest());
		}
		return cacheImageStream;
	}

	private Bitmap decodeBitmap(RequestForDecode request, Bitmap bitmap) {
		boolean flip = false;

		int inWidth = bitmap.getWidth();
		int inHeight = bitmap.getHeight();
		ImageOptions imageOptions = request.data.imageOptions;

		Resizer target = request.targetSize;
		if (imageOptions.centerCrop || imageOptions.centerInside) {
			target = request.data.setting.getSize() != null ? request.data.setting.getSize()
					: request.data.getImageWrapper().getTargetSize();
		}
		Matrix matrix = calculateSampleSizeAndMatrix(request.data, bitmap, target);

		// Check if should Flip the bitmap
		if (flip) {
			matrix.postScale(-1, 1);
		}

		// Rotate the bitmap image if required
		if (request.data.setting.shouldRotateDegrees()) {
			if (request.data.setting.shouldRotatePivot()) {
				matrix.setRotate(imageOptions.rotationDegrees, imageOptions.rotationPivotX, imageOptions.rotationPivotY);
			}else {
				matrix.setRotate(imageOptions.rotationDegrees);
			}
		}

		if (imageOptions.centerCrop || imageOptions.centerInside) {
			if (imageOptions.centerInside) {
				float[] v = new float[9];
				matrix.getValues(v);
				float minPercentage = v[Matrix.MSCALE_X];
				target.width = (int) (minPercentage * bitmap.getWidth());
				target.height = (int) (minPercentage * bitmap.getHeight());
			}

			Bitmap result = BitmapUtils.createBitmap(target.width, target.height, BitmapUtils.getNonNullConfig(bitmap));//or can load from memory
			result.setHasAlpha(bitmap.hasAlpha());

			BitmapUtils.applyMatrix(bitmap, result, matrix);
			return result;
		}

		Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0,0, inWidth, inHeight, matrix, true);
		if (newBitmap != bitmap) {
			bitmap.recycle();
			bitmap = newBitmap;
		}
		return bitmap;
	}

	/**
	 * Calculate optimal down-sampling factor given the dimensions of a source
	 * image, the dimensions of a destination area and a scaling logic.
	 *
	 * @param srcWidth Width of source image
	 * @param srcHeight Height of source image
	 * @param dstWidth Width of destination area
	 * @param dstHeight Height of destination area
	 * @return Optimal down scaling sample size for decoding
	 */
	public static int calculateSampleSize(int srcWidth, int srcHeight, int dstWidth, int dstHeight,
										  ImageScale imageScale) {
		if (imageScale == ImageScale.FIT_XY) {
			final float srcAspect = (float)srcWidth / (float)srcHeight;
			final float dstAspect = (float)dstWidth / (float)dstHeight;

			if (srcAspect > dstAspect) {
				return srcWidth / dstWidth;
			} else {
				return srcHeight / dstHeight;
			}
		} else {
			final float srcAspect = (float)srcWidth / (float)srcHeight;
			final float dstAspect = (float)dstWidth / (float)dstHeight;

			if (srcAspect > dstAspect) {
				return srcHeight / dstHeight;
			} else {
				return srcWidth / dstWidth;
			}
		}
	}

	public Matrix calculateSampleSizeAndMatrix(final RequestData data, Bitmap bitmap, Resizer targetSize) {
		float sx = 1;
		float sy = 1;
		boolean stretch = false;
		int width = targetSize.getWidth();
		int height = targetSize.getHeight();

		if (data.imageOptions.centerCrop) {
			return BitmapUtils.centerCrop(null, bitmap, width, height);
		}
		else if (data.imageOptions.centerInside) {
			return BitmapUtils.centerInside(null, bitmap, width, height);
		}

		Matrix matrix = new Matrix();
		int inWidth = bitmap.getWidth();
		int inHeight = bitmap.getHeight();

		float scaleWidth = (float) inWidth / width;

		if (width != 0 && height != 0 //
				&& (width != inWidth || height != inHeight)) {
			float newsx = width / (float) inWidth;
			float newsy = height / (float) inHeight;
			sx = newsx;
			sy = newsy;

		} else {
			int tarWidth = width;
			int tarHeight = (int) (inHeight /scaleWidth);

			if ((!stretch && tarWidth < inWidth && tarHeight < inHeight) || (stretch && tarWidth != inWidth && tarHeight != inHeight)) {
				sx = (float) tarWidth / inWidth;
			}
			sy = sx;
		}
		matrix.setScale(sx, sy);
		return matrix;
	}

	@Override
	public boolean saveImage(RequestForDecode request,DiscMemoryCacheOptions options) throws IOException {
		boolean shouldCache = true;
		boolean success = false;
		String key = UriScheme.FILE.remove(request.imageKey);
		// Decode image file, compress and re-save it into storage
		Bitmap bitmap = decodeFile(request);//decode bitmap before saving it
		if (bitmap != null && shouldCache == true) {
			success = BitmapUtils.writeBitmapToFile(bitmap, key, options.bitmapCompressFormat);
			if(success) bitmap.recycle();//recycle bitmap
		}
		return success;
	}

	public static ImageScale getScaleType(ImageView imageView) {
		if (imageView != null) {
			return ImageScale.scaleView(imageView);
		}
		return null;
	}

	public static class RequestForDecode {
		public boolean updateLastModified;
		public Resizer maxSize;
		public Resizer targetSize;
		public final RequestData data;
		final ImageScale scaleType;
		final Options options;
		final String imageKey;
		final boolean canDecodeBitmap;

		public final boolean useDownloaderForInputStream;

		public RequestForDecode(String key,boolean useDownloadForStream,Resizer targetSize,Resizer maxResizer,Options options ,RequestData data) {
			this(key,useDownloadForStream,true,targetSize,maxResizer,options ,data);
		}

		public RequestForDecode(String key, boolean useDownloadForStream,boolean canDecodeBitmap,Resizer targetSize,Resizer maxResizer,Options options ,RequestData data) {
			this.maxSize = maxResizer;
			this.targetSize = targetSize;
			this.imageKey = key;
			this.options = options;
			this.data = data;
			this.useDownloaderForInputStream = useDownloadForStream;
			this.canDecodeBitmap = canDecodeBitmap;
			this.scaleType = data.getImageWrapper().getViewScaleType();
		}

		public File getFile() {
			return new File(UriScheme.FILE.remove(imageKey));
		}
	}

	public enum ImageScale {
		FIT_NORMAL, CROP, FIT_XY, CENTER;

		public static ImageScale scaleView(ImageView imageView) {
			switch (imageView.getScaleType()) {
				case FIT_CENTER:
				case FIT_XY:
					//	return FIT_XY;
				case FIT_START:
				case FIT_END:
				case CENTER_INSIDE:
					return FIT_NORMAL;
				case CENTER:
				case CENTER_CROP:
					return CROP;
				default:
					return CROP;
			}
		}
	}
}