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

import static android.provider.Settings.System.AIRPLANE_MODE_ON;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.freedom.asyncimageloader.StringWriter;
import com.freedom.asyncimageloader.assist.LengthInputStream;
import com.freedom.asyncimageloader.interfaces.HttpSocket;
import com.freedom.asyncimageloader.interfaces.SocketResponse;
import com.freedom.asyncimageloader.listener.CusListener;
import com.freedom.asyncimageloader.uri.URLEncoded.UriScheme;
import com.freedom.asyncimageloader.utils.Logg;
import com.freedom.asyncimageloader.utils.TimeUtils;
import com.freedom.asyncimageloader.utils.Utility;

import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.text.format.DateUtils;
import android.widget.Toast;
import android.text.*;
import android.app.*;

public class NetworkService implements NetworkManager {
	protected static final int TEMP_REDIRECT = 5;
	public static final int DEFAULT_THREAD_POOL_COUNT = 1;
	
	protected static final int READ_TIMEOUT = 20 * 1000;
	protected static final int CONNECT_TIMEOUT = 5 * 1000;
	protected static final int ERROR_CODE = 404;
	
	protected final Context context;
	Handler handler = new Handler();
	boolean loggingEnabled = true;
	private int downloadcount = 0;
	HttpSocket socket;
	
	public NetworkService(Context context) {
		this(context,new HttpSocket.SocketConnection().build());
	}
	
	public NetworkService(Context context,HttpSocket socket) {
		this.context = context.getApplicationContext();
		this.socket = socket;
	}
	
	@Override
	public void setHttpSocket(HttpSocket socket)  {
		this.socket = socket;
	}
	
	@Override
	public InputStream getFileStreamFromNetwork(final String url,CusListener.failedListener listener) throws IOException {
		HttpURLConnection conn = null;
		 disableConnectionReuseIfNecessary();
		    if(socket != null) {
		        conn = createConnection(socket,url);
		    } else {
		        conn = createConnection(url,READ_TIMEOUT,CONNECT_TIMEOUT);
		    }
		    int responseCode = conn.getResponseCode();			 
			int redirectCount = 0;
		    int errorcode = socket != null ? socket.getErrorCode() : ERROR_CODE;
			
			if (responseCode == errorcode) {
				Utility.closeStream(conn.getErrorStream());
				String error = "No file to download. Server replied HTTP code: " + responseCode;
				
			    if (socket != null) {	
				     if (loggingEnabled) Logg.debug(""+responseCode);

				     if (socket.callback != null)
					     socket.callback.onSocketError(new SocketResponse(responseCode,url,error));		
			     }
				 throw new IOException(error);
			 }
			 
			 while (conn.getResponseCode() / 100 == 3 && redirectCount < TEMP_REDIRECT) {
				conn = createConnection(conn.getHeaderField("Location"),READ_TIMEOUT,CONNECT_TIMEOUT);
				redirectCount++;
			 }
		     responseCode = conn.getResponseCode();
			 //always check HTTP response code first
			 if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				 downloadcount++;
				 String logs = returnRecieveLogs(responseCode,url,socket,conn);
				
			     if (socket != null) {
			          if (loggingEnabled) Logg.info(logs);
					
			          if (socket.callback != null) 
				          socket.callback.onSocketComplete(new SocketResponse(responseCode,url, logs));
			     }
			 }
			InputStream imageStream;
			try {
				imageStream = conn.getInputStream();
			} catch (IOException e) {
				Utility.closeStream(conn.getErrorStream());
				
				if (socket != null) {
					String error = "No file to download. Server replied HTTP code: " + responseCode+ " error "+e.getMessage();
					
				    if (loggingEnabled) Logg.error(e,error);
					
				    if (socket.callback != null)
				       socket.callback.onSocketError(new SocketResponse(responseCode,url,error));
				}
				throw e;
			}
			return new LengthInputStream(imageStream,conn.getContentLength());
		}

	    @Override
	    public HttpURLConnection createConnection(String url,int connectTimeout,int readTimeout) throws IOException {
	    	 URL Url = new URL(url);
		     HttpURLConnection conn = (HttpURLConnection) Url.openConnection();
		     conn.setConnectTimeout(connectTimeout);
		     conn.setReadTimeout(readTimeout);
		     conn.setInstanceFollowRedirects(true);
		  	 return conn;
		}

		@Override
	    public HttpURLConnection createConnection(HttpSocket socket,String url) throws IOException {
			URL imageUrl = new URL(url);	
			//HttpURLConnection.setFollowRedirects(socket.getFollowRedirects());
			
    		HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
            conn.setFollowRedirects(socket.getFollowRedirects());
			conn.setConnectTimeout(socket.connecttimeout);
            conn.setReadTimeout(socket.readtimeout);
            conn.setInstanceFollowRedirects(socket.instanceFollowRedirects);
	    	conn.setUseCaches(socket.useCaches);
			conn.setRequestMethod(socket.getRequestMethod());
			return conn;
		}

	    public HttpURLConnection redirectManually(HttpSocket socket,HttpURLConnection conn) throws IOException {
			conn = createConnection(conn.getHeaderField("Location"),READ_TIMEOUT,CONNECT_TIMEOUT);
	        return conn;
	    }
	   
	    public String returnRecieveLogs(int responseCode,String Url,HttpSocket socket, HttpURLConnection conn) {
			// always check HTTP response code first
			if (responseCode == HttpURLConnection.HTTP_OK) {
				String fileName = Url;
				String disposition = conn.getHeaderField("Content-Disposition");
				String contentType = conn.getContentType();
				int contentLength = conn.getContentLength();
				long lastModified = conn.getLastModified();
				long Expiration = conn.getExpiration();
				Uri uri = Uri.parse(Url);

				if (disposition != null) {
					// extracts file name from header field
					int index = disposition.indexOf("filename=");
					if (index > 0) {
						fileName = disposition.substring(index + 10,disposition.length() - 1);
					}
				} else {
					// extracts file name from URL
					fileName = Url.substring(Url.lastIndexOf("/") + 1,Url.length());
				}
				
				StringWriter logwriter = new StringWriter()
		        .append("===============START FOR RECIEVE SOCKET ===============")
		        .append("    Socket Receive   ")
				.append("ResponseCode: ",responseCode)
		        .append("Http ","  "+getType(uri.toString())+": ")
		        .append("Content-Type: ",contentType)
		        .append("Content-Disposition: ",disposition)
		        .append("Content-Length: ",contentLength)
		        .append("    File Description   ")
		        .append("site url: ",uri.getAuthority())
		        .append("file url path: ",uri.getPath())
		        .append("FileName: ",fileName)
		        .append("File Last Modified: ",TimeUtils.getMillsAgo(TimeUtils.getCalendarTime().getTime() - lastModified))
		        .append("File Expiration Date: ",DateUtils.formatDateTime(context,Expiration, 
			    		  DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL))
				.append("    Socket Connection   ")
				.append("Follow Redirects: ",conn.getFollowRedirects())
				.append("Request Method: ",conn.getRequestMethod())
		        .append("    Connection: ","close")
		        .append("===============END FOR RECIEVE SOCKET =================")
		        .flush();
              return logwriter.toString();
			}
			return null;
		}
	    
	private int getRange(String url) {
		try {
			HttpURLConnection.setFollowRedirects(false);
			int Count = 1;
			URL checkURL;
			while (true) {
				checkURL = new URL(url);
				HttpURLConnection con = (HttpURLConnection) checkURL.openConnection();
				con.setRequestMethod("HEAD");
				if (con.getResponseCode() == 404) {
					return Count - 2;
				}
				Logg.debug(con.getResponseCode()+"");
			  }
		   } catch (Exception e) {

		  }
		   return 1;
	   }
	
	    /**
	     * Simple network connection check.
	     */
	   
	    public boolean checkConnection() {
	    	return checkConnection(context);
	    }
	    
	    public boolean setloggingEnabled(boolean loggingEnabled) {
	    	return this.loggingEnabled = loggingEnabled;
	    }
	    
	    public String getType(String url) {
	    	switch (UriScheme.match(url)) {
			case HTTP: 
				return "HTTP";
			case HTTPS:
				return "HTTPS";
			default:
				return "HTTP";
			}
	    }
	    
	    public static boolean checkConnection(Context context) {
	        final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

	        final NetworkInfo networkInfo = cm.getActiveNetworkInfo();
	        if (networkInfo == null || !networkInfo.isConnectedOrConnecting()) {
	            Logg.warning("checkConnection - no connection found");
	            return false;
	        }
	        return true;
	    }
	    
	    public void disableConnectionReuseIfNecessary() {
	        // HTTP connection reuse which was buggy pre-froyo
	        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
	            System.setProperty("http.keepAlive", "false");
	        }
	    }
	    
	    /**
	     * Check if device airplane is on
	     */
	    @SuppressWarnings("deprecation")
		public boolean isAirplaneModeOn() {
	        ContentResolver contentResolver = context.getContentResolver();
	        return Settings.System.getInt(contentResolver, AIRPLANE_MODE_ON, 0) != 0;
	      }
}
