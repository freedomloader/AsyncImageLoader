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

import java.io.InputStream;

import com.freedom.asyncimageloader.interfaces.PhotoLoadFrom;

import android.graphics.Bitmap;

public class GetBitmap {
	public PhotoLoadFrom  from;
	public Bitmap bitmap; 
	public InputStream stream; 
    
    public GetBitmap(Bitmap bitmapDisplay,PhotoLoadFrom bitFrom){
        bitmap = bitmapDisplay;
        from = bitFrom;
    }
    
    public Bitmap getBitmap() {
    	return bitmap;
    }
    
    public PhotoLoadFrom getLoadedFrom() {
    	return from;
    }
}
