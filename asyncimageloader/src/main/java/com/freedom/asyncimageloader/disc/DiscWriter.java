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
package com.freedom.asyncimageloader.disc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.freedom.asyncimageloader.listener.CusListener.BytesListener;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

public interface DiscWriter{
    boolean writeFile(File cacheFile,Bitmap bitmap,CompressFormat compressFormat,int quality) throws IOException;
    boolean writeFile(File cacheFile,Bitmap bitmap) throws IOException;
    boolean writeOutput(InputStream is, OutputStream os,BytesListener bytesListener) throws IOException;
    
}
