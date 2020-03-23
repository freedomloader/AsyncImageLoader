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
import android.graphics.Bitmap.Config;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
	
  public class RoundedTransformBitmap implements Transform {
	    private final int radius;
	    private final int margin;  
	    private boolean drawCircle;  
		private int canvasSize;
		private int borderWidth = 32;
		protected BitmapShader bitmapShader;
		protected Paint paintBorder;
		protected RectF mBitmapRect = new RectF();
		private static int DEFAULT_MARGIN = 0;
	    
		public RoundedTransformBitmap(final int radius) {
	        this(radius,DEFAULT_MARGIN,false);
	    }
		
		public RoundedTransformBitmap(final int radius, final int margin) {
	        this(radius,margin,false);
	    }
		
	    public RoundedTransformBitmap(final int radius, final int margin,boolean drawCircle) {
	        this.radius = radius;
	        this.margin = margin;
	        this.drawCircle = drawCircle;
	    }

	    @Override
	    public Bitmap transformBitmap(final Bitmap originalBitmap) {
	    	bitmapShader = new BitmapShader(originalBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
			mBitmapRect = new RectF (margin, margin, originalBitmap.getWidth() - margin, originalBitmap.getHeight() - margin);
			
			paintBorder = new Paint();
			paintBorder.setAntiAlias(true);
			paintBorder.setShader(bitmapShader);
	        Bitmap output = Bitmap.createBitmap(originalBitmap.getWidth(), originalBitmap.getHeight(), Config.ARGB_8888);
			      
	        Canvas canvas = new Canvas(output);
	        canvasSize = canvas.getWidth();
			if(canvas.getHeight()<canvasSize) canvasSize = canvas.getHeight();
			
	        if(drawCircle) {
				int circleCenter = (canvasSize - (borderWidth * 2)) / 2;
				canvas.drawCircle(circleCenter + borderWidth, circleCenter + borderWidth, ((canvasSize - (borderWidth * 2)) / 2) + borderWidth - 4.0f, paintBorder);
			}else {
				canvas.drawRoundRect(new RectF(margin, margin, originalBitmap.getWidth() - margin, originalBitmap.getHeight() - margin),
				radius, radius, paintBorder);
			}
			
	        if (originalBitmap != output) {
	        	originalBitmap.recycle();
	        }
	        return output;
	    }

		@Override
		public String transformKey() {
			// TODO Auto-generated method stub
			return "rounded "+radius;
		}
}
