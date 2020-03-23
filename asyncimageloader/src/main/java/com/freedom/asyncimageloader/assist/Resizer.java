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

/** Size */
public class Resizer { 
	public boolean useSimilarSizeIfFound;
	public int width = 0;
	public int height = 0;
	public int maxHeight = 0;
	public int maxWidth = 0;

	/** UseSimilarSizeIfFound */
	public Resizer(boolean useSimilarSizeIfFound) {
		this.useSimilarSizeIfFound = useSimilarSizeIfFound;
	}
	
	/** Size */
	public Resizer(int width, int height) {
		setWidth(width);
		setHeight(height);
	}

	/** Size */
	public Resizer(int width, int height,int maxwidth, int maxheight) {
		setWidth(width);
		setHeight(height);
		setMaxWidth(maxwidth);
		setMaxHeight(maxheight);
	}
	
	/** set UseSimilarSizeIfFound */
	public void setUseSimilarSizeIfFound(boolean useSimilarSizeIfFound) {
		this.useSimilarSizeIfFound = useSimilarSizeIfFound;
	}
	
	/** setMax Width*/
	public void setMaxWidth(int maxwidth) {
		this.maxWidth = maxwidth;
	}
	
	/** setMax Height*/
	public void setMaxHeight(int maxheight) {
		this.maxHeight = maxheight;
	}
	
	/** setMax Width*/
	public void setWidth(int width) {
		this.width = width;
	}

	/** setMax Height*/
	public void setHeight(int height) {
		this.height = height;
	}
	
	/** Widget */
	public int getWidth() {
		return width;
	}

	/** Height */
	public int getHeight() {
		return height;
	}
	
	/**Max Widget */
	public int getMaxWidth() {
		return maxWidth;
	}

	/** Max Height */
	public int getMaxHeight() {
		return maxHeight;
	}
	
	/** UseSimilarSizeIfFound*/
	public boolean isUseSimilarSizeIfFound() {
		return useSimilarSizeIfFound;
	}
}
