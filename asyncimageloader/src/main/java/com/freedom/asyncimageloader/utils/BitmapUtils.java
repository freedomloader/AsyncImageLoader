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

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.animation.Interpolator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import android.graphics.*;
import android.widget.*;
import android.view.*;
import android.view.animation.*;

import com.freedom.asyncimageloader.cache.MemoryCache;

public class BitmapUtils {
	protected static final int IO_BUFFER_SIZE = 32 * 1024;
	private static final Bitmap.Config DEFAULT_CONFIG = Bitmap.Config.ARGB_8888;

	public static Matrix centerCrop(@NonNull MemoryCache cache, @NonNull Bitmap inBitmap, int width,
									int height) {
		if (inBitmap.getWidth() == width && inBitmap.getHeight() == height) {
			return new Matrix();
		}
		final float scale;
		final float dx;
		final float dy;
		Matrix m = new Matrix();
		if (inBitmap.getWidth() * height > width * inBitmap.getHeight()) {
			scale = (float) height / (float) inBitmap.getHeight();
			dx = (width - inBitmap.getWidth() * scale) * 0.5f;
			dy = 0;
		} else {
			scale = (float) width / (float) inBitmap.getWidth();
			dx = 0;
			dy = (height - inBitmap.getHeight() * scale) * 0.5f;
		}

		m.setScale(scale, scale);
		m.postTranslate((int) (dx + 0.5f), (int) (dy + 0.5f));
		return m;
	}

	public static Matrix fitCenter(@NonNull MemoryCache cache, @NonNull Bitmap inBitmap, int width,
								   int height) {
		if (inBitmap.getWidth() == width && inBitmap.getHeight() == height) {
			return new Matrix();
		}
		final float widthPercentage = width / (float) inBitmap.getWidth();
		final float heightPercentage = height / (float) inBitmap.getHeight();
		final float minPercentage = Math.min(widthPercentage, heightPercentage);

		int targetWidth = Math.round(minPercentage * inBitmap.getWidth());
		int targetHeight = Math.round(minPercentage * inBitmap.getHeight());

		if (inBitmap.getWidth() == targetWidth && inBitmap.getHeight() == targetHeight) {
			return new Matrix();
		}

		Matrix matrix = new Matrix();
		matrix.setScale(minPercentage, minPercentage);
		return matrix;
	}

	public static Matrix centerInside(@NonNull MemoryCache cache, @NonNull Bitmap inBitmap, int width,
									  int height) {
		if (inBitmap.getWidth() <= width && inBitmap.getHeight() <= height) {
			return new Matrix();
		} else {
			return fitCenter(cache, inBitmap, width, height);
		}
	}

	/**
	 * Convert bitmap to the GrayScale
	 * 
	 * @param orginalBitmap Original bitmap
	 * @return GrayScale bitmap
	 */
	public static Bitmap toGrayscale(Bitmap orginalBitmap) {
		final int height = orginalBitmap.getHeight();
		final int width = orginalBitmap.getWidth();
		final Bitmap bmpGrayscale = Bitmap.createBitmap(width, height,Bitmap.Config.ARGB_8888);
		final Canvas c = new Canvas(bmpGrayscale);
		final Paint paint = new Paint();
		final ColorMatrix cm = new ColorMatrix();
		cm.setSaturation(0);
		final ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
		paint.setColorFilter(f);
		c.drawBitmap(orginalBitmap, 0, 0, paint);
		return bmpGrayscale;
	}

	/**
	 * Convert bitmap to black and white
	 * 
	 * @param orginalBitmap Original bitmap
	 * @return blackAndWhite bitmap
	 */
	public static Bitmap toBlackandWhite(Bitmap orginalBitmap) {
		ColorMatrix colorMatrix = new ColorMatrix();
		colorMatrix.setSaturation(0);
		ColorMatrixColorFilter colorMatrixFilter = new ColorMatrixColorFilter(colorMatrix);
		Bitmap blackAndWhiteBitmap = orginalBitmap.copy(Bitmap.Config.ARGB_8888, true);
		Paint paint = new Paint();
		paint.setColorFilter(colorMatrixFilter);
		Canvas canvas = new Canvas(blackAndWhiteBitmap);
		canvas.drawBitmap(blackAndWhiteBitmap, 0, 0, paint);
		return blackAndWhiteBitmap;
	}
	
	/**
	 * Change bitmap to rounded corner
	 * 
	 * @param orginalBitmap Original bitmap
	 * @param pixels rounded pixels
	 * @return Rounded bitmap
	 */
	public static Bitmap toRoundedCorner(Bitmap orginalBitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(orginalBitmap.getWidth(), orginalBitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, orginalBitmap.getWidth(), orginalBitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(orginalBitmap, rect, rect, paint);

        return output;
    }
	
	/**
	 * Convert drawable to image icon
	 * 
	 * @param orginalBitmap
	 * @param color specific color
	 * @return design bitmap
	 */
	@SuppressWarnings("deprecation")
	public static Bitmap toDesignImageIcon(Bitmap orginalBitmap,int color,boolean invert) {
		Drawable src = new BitmapDrawable(orginalBitmap);
		int width = src.getIntrinsicWidth();
		int height = src.getIntrinsicHeight();
		if (width <= 0 || height <= 0) {
		throw new UnsupportedOperationException("Source drawable needs an intrinsic size.");
		}
		Bitmap bitmap = Bitmap.createBitmap(width , height, Bitmap.Config. ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		Paint colorToAlphaPaint = new Paint();
		int invMul = invert ? -1 : 1;
		colorToAlphaPaint.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix(new float[]{
		0 , 0, 0 , 0 , Color.red ( color ),
		0 , 0, 0 , 0 , Color.green ( color ),
		0 , 0, 0 , 0 , Color.blue( color ),
		invMul * 0.213f , invMul * 0.715f , invMul * 0.072f , 0 , invert ? 255 : 0 ,
		})));
		
		canvas.saveLayer(0,0,width,height,colorToAlphaPaint,Canvas.ALL_SAVE_FLAG );
		canvas.drawColor(invert ? Color.WHITE : Color.BLACK );
		src.setBounds(0 , 0 , width , height );
		src.draw(canvas);
		canvas.restore();
		return bitmap ;
	 }
	
	/**
	 * Convert string to bitmap
	 * 
	 * @param encodedString
	 * @return bitmap (from given string)
	 */
	 public static Bitmap StringToBitMap(String encodedString) {
	   try {
		 byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
		 Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0,encodeByte.length);
		 return bitmap;
	   } catch (Exception e) {
		 e.getMessage();
		 return null;
	   }
	  }
		 
	/**
	 * Convert bitmap to string
	 * 
	 * @param bitmap
	 * @return string (from given bitmap)
	 */
	 public static String BitMapToString(Bitmap bitmap) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100,baos);
		byte[] b = baos.toByteArray();
		String temp = Base64.encodeToString(b,Base64.DEFAULT);
		return temp;
	 }
	 
	 /**
	  * Writes a bitmap to a file. Call {writeBitmapToFile(Bitmap,String,CompressFormat)}
	  * first to set the target bitmap compression and format.
	  *
	  * @param bitmap
	  * @param file
	  */
	  public static boolean writeBitmapToFile(Bitmap bitmap, String file,CompressFormat compressFormat) 
		 throws IOException, FileNotFoundException {
         OutputStream out = null;
	     try {
	         out = new BufferedOutputStream(new FileOutputStream(file), IO_BUFFER_SIZE);
	         return bitmap.compress(compressFormat, 100, out);
	     } finally {
	         if (out != null) {
	             out.close();
	         }
	     }
	}
	  
	public static Bitmap drawableToBitmap (Drawable drawable) {
		Bitmap bitmap = null;

		if (drawable instanceof BitmapDrawable) {
			BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
			if(bitmapDrawable.getBitmap() != null) {
				return bitmapDrawable.getBitmap();
			}
		}

		if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
			bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
		} else {
			bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
		}

		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);
		return bitmap;
	}

	public static Bitmap toRoundedCroppedBitmap(Bitmap bitmap, int radius) {
		Bitmap finalBitmap;
		if (bitmap.getWidth() != radius || bitmap.getHeight() != radius)
			finalBitmap = Bitmap.createScaledBitmap(bitmap, radius, radius,
													false);
		else
			finalBitmap = bitmap;
		Bitmap output = Bitmap.createBitmap(finalBitmap.getWidth(),
											finalBitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, finalBitmap.getWidth(),
								   finalBitmap.getHeight());

		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
		paint.setDither(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(Color.parseColor("#BAB399"));
		canvas.drawCircle(finalBitmap.getWidth() / 2 + 0.7f,
						  finalBitmap.getHeight() / 2 + 0.7f,
						  finalBitmap.getWidth() / 2 + 0.1f, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(finalBitmap, rect, rect, paint);

		return output;
	}

	public static Bitmap toOvalCroppedBitmap(Bitmap bitmap, int radius) {
		Bitmap finalBitmap;
		if (bitmap.getWidth() != radius || bitmap.getHeight() != radius)
			finalBitmap = Bitmap.createScaledBitmap(bitmap, radius, radius,
													false);
		else
			finalBitmap = bitmap;
		Bitmap output = Bitmap.createBitmap(finalBitmap.getWidth(),
											finalBitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, finalBitmap.getWidth(),
								   finalBitmap.getHeight());

		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
		paint.setDither(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(Color.parseColor("#BAB399"));
		RectF oval = new RectF(0, 0, 130, 150);
		canvas.drawOval(oval, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(finalBitmap, rect, oval, paint);

		return output;
	}

	public static Bitmap toTriangleCroppedBitmap(Bitmap bitmap, int radius) {
		Bitmap finalBitmap;
		if (bitmap.getWidth() != radius || bitmap.getHeight() != radius)
			finalBitmap = Bitmap.createScaledBitmap(bitmap, radius, radius,
													false);
		else
			finalBitmap = bitmap;
		Bitmap output = Bitmap.createBitmap(finalBitmap.getWidth(),
											finalBitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, finalBitmap.getWidth(),
								   finalBitmap.getHeight());

		Point point1_draw = new Point(75, 0);
		Point point2_draw = new Point(0, 180);
		Point point3_draw = new Point(180, 180);

		Path path = new Path();
		path.moveTo(point1_draw.x, point1_draw.y);
		path.lineTo(point2_draw.x, point2_draw.y);
		path.lineTo(point3_draw.x, point3_draw.y);
		path.lineTo(point1_draw.x, point1_draw.y);
		path.close();
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(Color.parseColor("#BAB399"));
		canvas.drawPath(path, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(finalBitmap, rect, rect, paint);

		return output;
	}

	public static Bitmap toHexagonCroppedBitmap(Bitmap bitmap, int radius) {
		Bitmap finalBitmap;
		if (bitmap.getWidth() != radius || bitmap.getHeight() != radius)
			finalBitmap = Bitmap.createScaledBitmap(bitmap, radius, radius,
													false);
		else
			finalBitmap = bitmap;
		Bitmap output = Bitmap.createBitmap(finalBitmap.getWidth(),
											finalBitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, finalBitmap.getWidth(),
								   finalBitmap.getHeight());

		Point point1_draw = new Point(75, 0);
		Point point2_draw = new Point(0, 50);
		Point point3_draw = new Point(0, 100);
		Point point4_draw = new Point(75, 150);
		Point point5_draw = new Point(150, 100);
		Point point6_draw = new Point(150, 50);

		Path path = new Path();
		path.moveTo(point1_draw.x, point1_draw.y);
		path.lineTo(point2_draw.x, point2_draw.y);
		path.lineTo(point3_draw.x, point3_draw.y);
		path.lineTo(point4_draw.x, point4_draw.y);
		path.lineTo(point5_draw.x, point5_draw.y);
		path.lineTo(point6_draw.x, point6_draw.y);

		path.close();
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(Color.parseColor("#BAB399"));
		canvas.drawPath(path, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(finalBitmap, rect, rect, paint);

		return output;
	}
	
	public static int getHotspotColor (Bitmap img, int x, int y) {
		return img.getPixel(x, y);
	}
	
	public static int getHotspotColor (ImageView img, int x, int y) {
		img.setDrawingCacheEnabled(true); 
		Bitmap hotspots = Bitmap.createBitmap(img.getDrawingCache()); 
		img.setDrawingCacheEnabled(false);
		return hotspots.getPixel(x, y);
	}
	
	public static boolean closeBitmapMatch(int color1, int color2, int tolerance) {
		if ((int) Math.abs (Color.red (color1) - Color.red (color2)) > tolerance ) 
			return false;
		if ((int) Math.abs (Color.green (color1) - Color.green (color2)) > tolerance ) 
			return false;
		if ((int) Math.abs (Color.blue (color1) - Color.blue (color2)) > tolerance ) 
			return false;
		return true;
	} // end match
	
	public static Bitmap combineImagesLeft(Bitmap c, Bitmap s,boolean inSameView) { // can add a 3rd parameter 'String loc' if you want to save the new image - left some code to do that at the bottom 
		Bitmap cs = null; 

		if (inSameView) {
			cs = Bitmap.createBitmap(c.getWidth(), c.getHeight(), s.getConfig());
			Canvas canvas = new Canvas(cs);
			canvas.drawBitmap(c, 0f, 0f, null);
			canvas.drawBitmap(s, 30, 30, null);

		} else {
			int width, height = 0; 

			if(c.getWidth() > s.getWidth()) { 
				width = c.getWidth() + s.getWidth(); 
				height = c.getHeight(); 
			} else { 
				width = s.getWidth() + s.getWidth(); 
				height = c.getHeight(); 
			} 
			cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); 

			Canvas comboImage = new Canvas(cs); 
			comboImage.drawBitmap(c, 0f, 0f, null); 
			comboImage.drawBitmap(s, c.getWidth(), 0f, null); 
		}
		return cs; 
	} 
	
	public static Bitmap combineImagesTop(Bitmap c, Bitmap s,boolean inSameView) { // can add a 3rd parameter 'String loc' if you want to save the new image - left some code to do that at the bottom 
		Bitmap cs = null; 

		if (inSameView) {
			cs = Bitmap.createBitmap(c.getWidth(), c.getHeight(), s.getConfig());
			Canvas canvas = new Canvas(cs);
			canvas.drawBitmap(c, 0f, 0f, null);
			canvas.drawBitmap(s, 30, 30, null);

		} else {
			int width, height = 0; 

			if(c.getHeight() > s.getHeight()) { 
				width = c.getWidth(); 
				height = c.getHeight() + s.getHeight(); 
			} else { 
				width = c.getWidth(); 
				height = s.getHeight() + s.getHeight(); 
			} 
			cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); 

			Canvas comboImage = new Canvas(cs); 
			comboImage.drawBitmap(c, 0f, 0f, null); 
			comboImage.drawBitmap(s, 0f, c.getHeight(), null); 
		}
		return cs; 
	} 
	
	public static int findColor(View v) {
		// Get the bitmap from the view.
		ImageView imageView = (ImageView)v;
		BitmapDrawable bitmapDrawable = (BitmapDrawable)imageView.getDrawable();
		Bitmap imageBitmap = bitmapDrawable.getBitmap();
		return findColor(imageBitmap,v,0,0);
	}
	
	/**
     * Find components of color of the bitmap at x, y. 
     * @param x Distance from left border of the View
     * @param y Distance from top of the View
     * @param v Touched surface on screen
     */
	public static int findColor(Bitmap imageBitmap,View v, int x, int y) 
	throws NullPointerException {

		int red = 0;
		int green = 0;
		int blue = 0;
		int color = 0;

		int offset = 1; // 3x3 Matrix
		int pixelsNumber = 0;

		int xImage = 0;
		int yImage = 0;

        // Calculate the target in the bitmap.
		xImage = (int)(x * ((double)imageBitmap.getWidth() / (double)v.getWidth()));
		yImage = (int)(y * ((double)imageBitmap.getHeight() / (double)v.getHeight()));

        // Average of pixels color around the center of the touch.
		for (int i = xImage - offset; i <= xImage + offset; i++) {
			for (int j = yImage - offset; j <= yImage + offset; j++) {
				try {
					color = imageBitmap.getPixel(i, j);
					red += Color.red(color);
					green += Color.green(color);
					blue += Color.blue(color);
					pixelsNumber += 1;
				} catch(Exception e) {
					//Log.w(TAG, "Error picking color!");
				}  
			}
		}
		red = red / pixelsNumber;
		green = green / pixelsNumber;
		blue = blue / pixelsNumber;

		return Color.rgb(red, green, blue); 
	}

	@NonNull
	public static Bitmap.Config getNonNullConfig(@NonNull Bitmap bitmap) {
		return bitmap.getConfig() != null ? bitmap.getConfig() : Bitmap.Config.ARGB_8888;
	}

	public static void applyMatrix(@NonNull Bitmap inBitmap, @NonNull Bitmap targetBitmap,
									Matrix matrix) {
		try {
			final int PAINT_FLAGS = Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG;

			Canvas canvas = new Canvas(targetBitmap);
			canvas.drawBitmap(inBitmap, matrix, new Paint(PAINT_FLAGS));

			canvas.setBitmap(null);
		} catch (Exception e) {

		}
	}

	@NonNull
	public static Bitmap createBitmap(int width, int height, @Nullable Bitmap.Config config) {
		return Bitmap.createBitmap(width, height, config != null ? config : DEFAULT_CONFIG);
	}

	/**
	 * @param duration translate duration time
	 * @param from
	 * @param to
	 * @return TranslateAnimation
	 */
	public static Animation tanim(int duration,int from,int to) {
   	    return tanim(duration,0,from,0,to);
	}

	/**
	 * @param duration translate duration time
	 * @param fromType
	 * @param from
	 * @param toType
	 * @param to
	 * @return TranslateAnimation
	 */
	public static Animation tanim(int duration,int fromType,int from,int toType,int to) {
		TranslateAnimation anim = new TranslateAnimation(fromType,from,toType,to, 0, 0, 0, 0);
   	    anim.setDuration(duration);
   	    return anim;
	}

	/**
	 * @param duration scale duration time
	 * @param fromX
	 * @param toX
	 * @param fromY
	 * @param toY
	 * @param i
	 * @return ScaleAnimation
	 */
	public static Animation sanim(int duration,int fromX,int toX,int fromY,int toY, Interpolator i) {
   	    return sanim(duration,fromX,toX,fromY,toY,0, 0,i);
	}

	/**
	 * @param duration scale duration time
	 * @param fromX
	 * @param toX
	 * @param fromY
	 * @param toY
	 * @param pivotX
	 * @param pivotY
	 * @param i
	 * @return ScaleAnimation
	 */
	public static Animation sanim(int duration,int fromX,int toX,int fromY,int toY,int pivotX,int pivotY,Interpolator i) {
		ScaleAnimation anim = new ScaleAnimation(fromX,toX,fromY,toY, 0, pivotX, 0, pivotY);
		if(i != null) {
			anim.setInterpolator(i);
		}
   	    anim.setDuration(duration);
   	    return anim;
	}

	/**
	 * @param duration rotate duration time
	 * @param rcount animation repeat count
	 * @param rmode animation repeat mode
	 * @param from
	 * @param to
	 * @param pivotX
	 * @param pivotY
	 * @param i
	 * @return RotateAnimation
	 */
	public static Animation ranim(int duration,int rcount,int rmode,int from,int to,int pivotX,int pivotY,Interpolator i) {
		RotateAnimation anim = new RotateAnimation(from,to,0,pivotX, 0,pivotY);
		if(i != null) {
			anim.setInterpolator(i);
		}
   	    anim.setDuration(duration);
   	    anim.setRepeatCount(rcount);
   	    anim.setRepeatMode(rmode);
   	    return anim;
	}

	/**
	 * @param duration alpha duration time
	 * @param rcount animation repeat count
	 * @param rmode animation repeat mode
	 * @param from
	 * @param to
	 * @param i
	 * @return AlphaAnimation
	 */
	public static Animation aanim(int duration,int rcount,int rmode,int from,int to,Interpolator i) {
		AlphaAnimation anim = new AlphaAnimation(from,to);
		if(i != null) {
			anim.setInterpolator(i);
		}
   	    anim.setDuration(duration);
   	    anim.setRepeatCount(rcount);
   	    anim.setRepeatMode(rmode);
   	    return anim;
	}
}

