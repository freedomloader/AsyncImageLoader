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
package com.freedom.asyncimageloader.transform;

import com.freedom.asyncimageloader.transform.Transform;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
	
  public class ColorTransformBitmap implements Transform {
	    private final int color;  
	    private boolean invert;  
	    
		public ColorTransformBitmap(final int color) {
	        this(color,false);
	    }
		
	    public ColorTransformBitmap(final int color, boolean invert) {
	        this.color = color;
	        this.invert = invert;
	    }

	    @SuppressWarnings("deprecation")
		@Override
	    public Bitmap transformBitmap(final Bitmap originalBitmap) {
	    	Drawable src = new BitmapDrawable(originalBitmap);
			int width = src.getIntrinsicWidth();
			int height = src.getIntrinsicHeight();
			if (width <= 0 || height <= 0) {
			    throw new UnsupportedOperationException("Source drawable needs an intrinsic size.");
			}
			Bitmap bitmap = Bitmap.createBitmap(width , height, Bitmap.Config. ARGB_8888);
			Canvas canvas = new Canvas(bitmap);
			Paint colorPaint = new Paint();
			int invMul = invert ? -1 : 1;
			colorPaint.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix(new float[]{
			0 , 0, 0 , 0 , Color.red(color),
			0 , 0, 0 , 0 , Color.green(color),
			0 , 0, 0 , 0 , Color.blue(color),
			invMul * 0.213f , invMul * 0.715f , invMul * 0.072f , 0 , invert ? 255 : 0 ,})));
			
			canvas.saveLayer(0,0,width,height,colorPaint,Canvas.ALL_SAVE_FLAG);
			canvas.drawColor(invert ? Color.WHITE : Color.BLACK);
			src.setBounds(0 , 0 , width , height);
			src.draw(canvas);
			canvas.restore();
			return bitmap ;
	    }
	    
	    @Override
	    public String transformKey() {
			return "color: "+color;
	    }
}
