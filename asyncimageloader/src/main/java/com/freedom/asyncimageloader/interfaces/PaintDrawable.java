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
package com.freedom.asyncimageloader.interfaces;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

public class PaintDrawable extends Drawable {

	protected float cornerRadius;
	Bitmap image;
	private int canvasSize;
	public static final int DEFAULT_BORDER_COLOR = Color.BLACK;

	private int borderWidth = 0;
	protected final BitmapShader bitmapShader;
	protected final Paint paintBorder;

	protected int bitmapWidth = 0;
	protected int bitmapHeight = 0;
	protected int margin = 0;

	public static int CUSTOM_ROUNDED = 11111111;

	protected final RectF mRect = new RectF(), mBitmapRect;

	public PaintDrawable(Bitmap bitmap, int cornerRadius) {
		this.image = bitmap;
		this.cornerRadius = cornerRadius;
		this.bitmapWidth = bitmap.getWidth();
		this.bitmapHeight = bitmap.getHeight();

		bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
		mBitmapRect = new RectF (margin, margin, bitmap.getWidth() - margin, bitmap.getHeight() - margin);
		
		paintBorder = new Paint();
		paintBorder.setAntiAlias(true);
		paintBorder.setShader(bitmapShader);
	}
	 
	 @Override
		protected void onBoundsChange(Rect bounds) {
			super.onBoundsChange(bounds);
            mRect.set(margin, margin, bounds.width() - margin, bounds.height() - margin);
			
			// Resize the original bitmap to fit the new bound
			Matrix shaderMatrix = new Matrix();
			shaderMatrix.setRectToRect(mBitmapRect, mRect, Matrix.ScaleToFit.FILL);
			bitmapShader.setLocalMatrix(shaderMatrix);
			
		}
	 
	@Override
	  public void draw(Canvas canvas) {
		canvasSize = canvas.getWidth();
		if(canvas.getHeight()<canvasSize)
		canvasSize = canvas.getHeight();

		// circleCenter is the x or y of the view's center
		// radius is the radius in pixels of the cirle to be drawn
		// paint contains the shader that will texture the shape
		if (cornerRadius == CUSTOM_ROUNDED) {
			int circleCenter = (canvasSize - (borderWidth * 2)) / 2;
			canvas.drawCircle(circleCenter + borderWidth, circleCenter + borderWidth, ((canvasSize - (borderWidth * 2)) / 2) + borderWidth - 4.0f, paintBorder);
		} else {
			canvas.drawRoundRect(mRect, cornerRadius, cornerRadius, paintBorder);
		}
	}
  

	@Override
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}

	@Override
	public void setAlpha(int alpha) {
		paintBorder.setAlpha(alpha);
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		paintBorder.setColorFilter(cf);
	}
	
	public float getCornerRadius() {
	    return cornerRadius;
	  }

	 public PaintDrawable setCornerRadius(float radius) {
		 this.cornerRadius = radius;
  	     update();
		 return this;
	 }
	
	public int getBorderWidth() {
	    return borderWidth;
	  }
	 
    public PaintDrawable setBorderWidth(int borderWidth) {
	    this.borderWidth = borderWidth;
	    paintBorder.setStrokeWidth(borderWidth);
 	     update();
		 return this;
	}

    public PaintDrawable setBorderColor(int borderColor) {
	  if (paintBorder != null)
	     paintBorder.setColor(borderColor);
	     update();
		 return this;
	}
    
    public PaintDrawable setMargin(int margin) {
  	     this.margin = margin;
  	     update();
  		 return this;
  	}
    
    void update() {
	     this.invalidateSelf();
	     this.copyBounds();
    }
    
    @SuppressLint({ "NewApi", "InlinedApi" })
	public PaintDrawable addShadow(ImageView view) {
    	view.setLayerType(View.LAYER_TYPE_SOFTWARE, paintBorder);
		paintBorder.setShadowLayer(4.0f, 0.0f, 2.0f, Color.BLACK);
 	     update();
		return this;
	}
}