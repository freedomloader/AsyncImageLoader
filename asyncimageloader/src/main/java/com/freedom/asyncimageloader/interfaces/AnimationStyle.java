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

import android.view.animation.Animation;

/** Animation */
public class AnimationStyle {

    public int duration = 0;
    Animation animation = null;
	AnimStyle animStyle = null;
	
	/**  Animation */
	public AnimationStyle(Animation animation) {
		this.animation = animation;
	}
	
	/**  Animation */
	public AnimationStyle(int duration) {
		this.duration = duration;
	}
	
	/**  Animation */
	public AnimationStyle(AnimStyle animStyle) {
		this.animStyle = animStyle;
	}

	/** Get Duration */
	public int getDuration() {
	    return duration;
	}
	
	/** Get Animation */
	public Animation getAnimation() {
	    return animation;
	}
	 
	/** Style */
	public AnimStyle getStyle() {
	    return animStyle;
	}
}
