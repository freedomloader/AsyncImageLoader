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
package com.freedom.asyncimageloader.network;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import com.freedom.asyncimageloader.interfaces.HttpSocket;
import com.freedom.asyncimageloader.listener.CusListener;

public interface NetworkManager {
    InputStream getFileStreamFromNetwork(String url,CusListener.failedListener listener) throws IOException;
	void setHttpSocket(HttpSocket socket);
    boolean checkConnection();
    boolean isAirplaneModeOn();
    void disableConnectionReuseIfNecessary();
    HttpURLConnection createConnection(HttpSocket socket,String url) throws IOException;
    HttpURLConnection createConnection(String url,int connectTimeout,int readTimeout) throws IOException;
}