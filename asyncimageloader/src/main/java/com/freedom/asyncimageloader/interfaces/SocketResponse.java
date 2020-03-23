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

import com.freedom.asyncimageloader.StringWriter;

public class SocketResponse {
    private int status;
    private String url;
    private String Content;

    public SocketResponse(int status,String url,String Content) {
        this.status = status;
        this.url = url;
        this.Content = Content;
    }
    
    public SocketResponse(int status,String url) {
        this.status = status;
        this.url = url;
    }

    public int getStatus() {
        return status;
    }
    
    public String getUrl() {
        return url;
    }
    
    public String getContent() {
        return Content;
    }
    
	@Override
	public String toString() {
		StringWriter stringAppend = new StringWriter()
        .append(" status: ",getStatus())
        .append(" text: ",getUrl())
        .append(" Content: ",getContent())
        .flush();
		return stringAppend.toString();
	}
}
