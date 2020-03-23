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
package com.freedom.asyncimageloader;

import com.freedom.asyncimageloader.interfaces.AnimationStyle;

public class ImageOptions {
    public boolean fit = false;
    public boolean centerCrop = false;
    public boolean centerInside = false;
    public int rounded = 0;
    public int fadein = 0;
    public int rotationDegrees = 0;
    public int rotationPivotX = 0;
    public int rotationPivotY = 0;
    public boolean grayScale = false;
    public boolean blackAndWhite = false;
    public AnimationStyle animation = null;
	
    public ImageOptions() { }
    
    void clearOptions() {
        this.rotationDegrees = 0;
    	this.fadein = 0;
    	this.rounded = 0;
    	this.rotationPivotX = 0;
    	this.rotationPivotY = 0;
    	this.animation = null;
    }
}
