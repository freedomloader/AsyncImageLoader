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
package com.freedom.asyncimageloader.listener;

import android.widget.ImageView;
import com.freedom.asyncimageloader.interfaces.DataEmiter;
import com.freedom.asyncimageloader.interfaces.Failed;

/** Callback for Custom Image Loader events. */
public interface LoaderCallback {
	/**
	 * set when bitmap was successful loaded. 
	 *
	 * @param dataEmiter    the given URLEncoded bitmap Loaded bitmap
	 * @param view  ImageView set.
	 */
	void onSuccess(DataEmiter dataEmiter, ImageView view);
	
	/**
	 * set when an image has failed to load. 
	 *
	 * @param fail why loading failed
	 * @param view  ImageView set.
	 */
    void onFailed(Failed fail, String uri, ImageView view);

    public class Adapter implements LoaderCallback {

		@Override
		public void onSuccess(DataEmiter dataEmiter, ImageView view) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onFailed(Failed fail, String uri, ImageView view) {
			// TODO Auto-generated method stub
			
		}

    }
}
