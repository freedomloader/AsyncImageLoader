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
package com.freedom.asyncimageloader.uri;

public interface UriUtil {
	  /** A lookup uri (e.g. content://com.android.contacts/contacts/lookup/3570i61d948d30808e537) */
	  static final int LOOKUP = 1;
	  /** A contact uri (e.g. content://com.android.contacts/contacts/38) */
	  static final int CONTACT = 3;

	  static final int DISPLAY_PHOTO = 4;

	  public static final String JPG_EXTENSION = ".jpg";
	  public static final String PNG_EXTENSION = ".png";
	  public static final String BMP_EXTENSION = ".bmp";
	  public static final String GIF_EXTENSION = ".gif";
	  public static final String FREEDOM_EXTENSION = ".freedom";
 }
