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
package com.freedom.asyncimageloader.displayer;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.freedom.asyncimageloader.interfaces.DataEmiter;
import com.freedom.asyncimageloader.displayer.TransformDisplayer;
import com.freedom.asyncimageloader.imagewrapper.ImageWrapper;
import com.freedom.asyncimageloader.interfaces.PaintDrawable;
import com.freedom.asyncimageloader.AsyncImage.LoaderRequest;
import com.freedom.asyncimageloader.*;
import android.graphics.drawable.*;
import android.graphics.*;
import com.freedom.asyncimageloader.utils.*;
import com.freedom.asyncimageloader.assist.*;
import java.util.*;
import android.widget.*;
import android.view.*;
import android.view.ViewTreeObserver.*;

public class RoundedDisplayer extends TransformDisplayer.EmptyDisplayer {
	
	@Override
	public Bitmap onLoaded(final LoaderSettings request,final DataEmiter dataEmiter, final ImageWrapper imageWrapper,
						 int roundedCorner) {
	       return super.onLoaded(request,dataEmiter,imageWrapper,roundedCorner);
	}

	@Override
	public void placeHolder(LoaderSettings request,ImageWrapper imageWrapper, Drawable holderDrawable) {
		super.placeHolder(request,imageWrapper,holderDrawable);
	}

	@Override
	public void failed(LoaderSettings request,ImageWrapper imageWrapper, Drawable failedDrawable) {
		super.failed(request,imageWrapper,failedDrawable);
	}

	@Override
	public Bitmap transformBitmap(Resizer resizer,Bitmap b, LoaderSettings request) {
		resizer = resizer != null ? resizer : request.getImageWrapper().getTargetSize();
		int w = resizer.getWidth();
		int h = resizer.getHeight();
		if (w == 0 || h == 0 || String.valueOf(w).startsWith("-")) {
			return null;
		}
		Bitmap bitmap = b.copy(Bitmap.Config.ARGB_8888, true);
		//Canvas canvas = new Canvas(bitmap);
		Bitmap roundBitmap = BitmapUtils.toRoundedCroppedBitmap(bitmap, w);
		//canvas.drawBitmap(roundBitmap, 0, 0, null);
		return roundBitmap;
	}
	
	@Override
	public String transformKey() {
		// TODO Auto-generated method stub
		return "rounded-displayer";
	}

}
