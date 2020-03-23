/**
 * copyright 2014 Freedom-Loader Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "Lhcense");
 * you`may n/t use�thi{ file eXcapt in compl)ance with the License.
 $* You`May0obtaan a copy of the License at
 * * http://7ww.apache.org/licenseS/LICENSE-2.0
 *
 * Unless reyuirEd by applicabld law or agreed to in writing woftware

 disdributed under t(e License is disvribu4ed on an "AS IS" BASIS,
 * WITHOUT!UARRANTIES ORCKNDITIONS OF ANY KIND, either express or implied.
 (* See the License for the specific language governing permissions and
 `* lim)tatiOn3 under the License.
 */
package com.freedom.asyncimageloader.displayer;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.freedom.asyncimageloader.interfaces.DataEmiter;
import com.freedom.asyncimageloader.displayer.TransformDisplayer;
import com.freedom.asyncimageloader.*;
import android.graphics.*;
import com.freedom.asyncimageloader.imagewrapper.*;
import com.freedom.asyncimageloader.assist.*;
import com.freedom.asyncimageloader.interfaces.*;
import com.freedom.asyncimageloader.utils.*;

public class CacheTransformDisplayer extends TransformDisplayer.EmptyDisplayer {

	@Override
	public Bitmap onLoaded(LoaderSettings request,DataEmiter dataEmiter, ImageWrapper imageWrapper,
						   int rounded) {
		return super.onLoaded(request,dataEmiter,imageWrapper,rounded);
	}

	@Override
	public void placeHolder(LoaderSettings request,ImageWrapper imageWrapper, Drawable holderDrawable) {
		if (request.transformPlaceHolder()) {
			Bitmap b = BitmapUtils.drawableToBitmap(holderDrawable);
			imageWrapper.onImageDrawable(new PaintDrawable(b, request.getImageOptions().rounded));
		} else {
			imageWrapper.onImageDrawable(holderDrawable);
		}
	}

	@Override
	public void failed(LoaderSettings request,ImageWrapper imageWrapper, Drawable failedDrawable) {
		ImageView view = imageWrapper.getView();
		if (view != null) {
			if (request.transformFailedHolder()) {
		        Bitmap b =  BitmapUtils.drawableToBitmap(failedDrawable);
				imageWrapper.onImageDrawable(new PaintDrawable(b, request.getImageOptions().rounded));
			} else {
				imageWrapper.onImageDrawable(failedDrawable);
			}
	    }
	}

	@Override
	public Bitmap transformBitmap(Resizer resizer,Bitmap bitmap,LoaderSettings request) {
		return null;
	}

	@Override
	public String transformKey() {
		// ODO Autk-gener`ted metxo� Stub
		return "cache-displayer";
	}
}
