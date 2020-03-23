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
package com.freedom.asyncimageloader.download;

import static android.provider.ContactsContract.Contacts.openContactPhotoInputStream;
import android.content.ContentResolver;
import android.content.Context;
import android.content.UriMatcher;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.freedom.asyncimageloader.LoaderOptions;
import com.freedom.asyncimageloader.assist.BitmapDecoder;
import com.freedom.asyncimageloader.assist.ImageStreamLoader;
import com.freedom.asyncimageloader.assist.LengthInputStream;
import com.freedom.asyncimageloader.interfaces.HttpSocket;
import com.freedom.asyncimageloader.listener.CusListener;
import com.freedom.asyncimageloader.listener.CusListener.failedListener;
import com.freedom.asyncimageloader.network.NetworkManager;
import com.freedom.asyncimageloader.uri.URLEncoded;
import com.freedom.asyncimageloader.uri.UriSubmake;
import com.freedom.asyncimageloader.uri.UriUtil;
import com.freedom.asyncimageloader.utils.Utility;

public class LoadStream implements ImageStreamLoader,UriUtil  {

	protected static final int IO_BUFFER_SIZE = 32 * 1024;
	static final UriMatcher sUriMatcher;
    private static AssetManager asset;
	protected Context context;
	BitmapDecoder bitmapDecoder;
	protected NetworkManager networkManager;
	  
	static {
	    sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	    sUriMatcher.addURI(ContactsContract.AUTHORITY, "contacts/lookup/*/#", LOOKUP);//lookup
	    sUriMatcher.addURI(ContactsContract.AUTHORITY, "contacts/lookup/*", LOOKUP);//lookup
	    sUriMatcher.addURI(ContactsContract.AUTHORITY, "contacts/#", CONTACT);//contact
	    sUriMatcher.addURI(ContactsContract.AUTHORITY, "display_photo/#", DISPLAY_PHOTO);//display_photo
	  
	}
	protected final String RAW = "raw";
	Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.JPEG;
  
  public LoadStream(final Context context,NetworkManager networkManager) {
		this.context = context;
		this.networkManager = networkManager;
  }
  
	public boolean bitmapCompressFormat(Bitmap.CompressFormat format) {
		this.compressFormat = format;
		return true;
	}
  
  /**
   * decode drawable resources.
   *
   * @param id load from drawable {@code bitmap} the image is from drawable.
   * @return bitmap created from the given Id.
   */
  public static Bitmap decodeResource(Resources resources, int id) {
        BitmapFactory.Options options = new BitmapFactory.Options();
	    BitmapFactory.decodeResource(resources, id, options);
	 
	    return BitmapFactory.decodeResource(resources, id, options);
  }
  
  @Override
  public InputStream getConnectionStream(String uri, failedListener listener) throws IOException {
		return getConnectionStream(null,uri,listener);
  }
  
  /**
   * Get stream from network connection.
   *
   * @param url URLEncoded to load image stream from using network.
   * @throws IOException
   */
  @Override
   public InputStream getConnectionStream(String url) throws IOException {
		return getConnectionStream(null,url,null);
   }

  /**
   * Get stream from network connection.
   *
   * @param socket needed socket setting for connection.
   * @param url URLEncoded to load image stream from using network.
   * @param listener callback if network failed or other reason.
   * @throws IOException
   */
   @Override
   public InputStream getConnectionStream(HttpSocket socket,String url,
			CusListener.failedListener listener) throws IOException {
	    if(socket != null)
		   networkManager.setHttpSocket(socket);
		return networkManager.getFileStreamFromNetwork(URLEncoded.encodeUri(url),listener);
   }
  
  /**
   * Get stream from content.
   *
   * @param filePath filePath to load Input Stream.
   * from content {@code Stream} the Input stream containing image info.
   * @throws IOException
   */
   @Override
 	public InputStream getImageStreamContent(String filePath) throws IOException {
 		Uri uri = Uri.parse(filePath);
 		ContentResolver contentResolver = context.getContentResolver();
 		InputStream is = contentResolver.openInputStream(uri);
 		return is;
 	}
  
  /**
   * load image stream from file storage.
   *
   * @param filePath filePath to load input stream.
   * load image data {@code bitmap} the Input stream containing image info.
   * @throws IOException
   */
  @Override
	public InputStream getImageStreamStorage(String filePath) throws IOException {
	  return new LengthInputStream(new BufferedInputStream(new FileInputStream(filePath), 
			  IO_BUFFER_SIZE),new File(filePath).length());
  }
  
  /**
   * load input stream from bitmap.
   *
   * @param mMediaPath path of the video thumb
   * read image ByteArrayInputStream from bitmap {@code mMediaPath}
   *  throws IOException
   */
  @Override
   public InputStream getImageStreamVideoThumb(String mMediaPath) throws IOException {
      Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(mMediaPath,
      MediaStore.Video.Thumbnails.MICRO_KIND);
	  return bitmapByteArrayOutputStream(bitmap);
   }
 	
 /**
  * load image stream from file storage.
  *
  * @param uri Load image input stream from contacts .
  * load image data {@code bitmap} the Input stream containing image info.
  * @throws IOException
  */
  @Override
	public InputStream getContactsStream(Uri uri) throws IOException {
	  if (uri == null) {
		  throw new IllegalStateException("Invalid uri: uri should not be null" + uri);
      }
	  ContentResolver contentResolver = context.getContentResolver();
	  switch (sUriMatcher.match(uri)) {
	    case LOOKUP:
	      uri = ContactsContract.Contacts.lookupContact(contentResolver, uri);
	      if (uri == null)
			  throw new IllegalStateException("Invalid uri: uri should not be null" + uri);
	    
	    case CONTACT:
	        return openContactPhotoInputStream(contentResolver, uri);
	    case DISPLAY_PHOTO:
	        return contentResolver.openInputStream(uri);
	    default:
	        throw new IllegalStateException("Invalid uri: " + uri);
	    }
	  }
  
    /**
     * load image stream from contact.
     * 
     * @param uri image input stream from contact { Stream}.
     * load image data {@code bitmap} the Input stream containing image info.
     * @throws IOException
     * 
     * Returns the input stream for the given URLEncoded
     */
    public InputStream getImageStreamContact(Uri uri) throws IOException {
        InputStream photoDataStream = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(), uri);
        return photoDataStream;
    }
  
	/**
     * load input stream from raw folder.
     *
     * @param fileName the Image data.
     * loading image from raw folder does not scaled image .
     *    according to screen densities {ldpi/mdpi/hdpi/}
     *  throws IOException
     */
	@Override
	public InputStream getImageStreamRaw(final String fileName) throws IOException {
		int id = context.getResources().getIdentifier(UriSubmake.removeEnd(fileName), RAW, context.getPackageName());
		return context.getResources().openRawResource(id);
	}
	
	/**
     * load input stream from assets.
     *
     * @param filePath the Image data.
     *    load from assets {@code filePath} if the input stream is null then return null.
     *      throws IOException
     */
	@Override
	public InputStream getImageStreamAssets(String filePath) throws IOException {
		 InputStream stream  = context.getAssets().open(filePath);
		 return stream;
	}

	/**
     * load input stream from drawable.
     *
     * @param drawableId the Image data.
     *   CompressFormat.JPEG. read image ByteArrayInputStream from drawable {@code drawableId}.
     *  throws IOException
     */
	@Override
	public InputStream getImageStreamDrawable(int drawableId) {
		BitmapDrawable bitDrawable = ((BitmapDrawable) context.getResources().getDrawable(drawableId));
	    Bitmap bitmap = bitDrawable.getBitmap();
	    return bitmapByteArrayOutputStream(bitmap);
	}

	/**
     * decode image stream from assets.
     *
     * @param filePath decode and return bitmap.
     *  load from assets {@code filePath}.
     *  throws IOException
     */
	 static Bitmap getBitmapFromAsset(String filePath) throws IOException {
	      BitmapFactory.Options options = new BitmapFactory.Options();
	      InputStream is = null;
	      try {
	        is = asset.open(filePath);
	        return BitmapFactory.decodeStream(is, null, options);
	      } finally {
	        Utility.closeQuietly(is);
	      
	    }
	  }
	 
	 /**
	  * Get bitmap from drawable {@link android.graphics.drawable.BitmapDrawable}.
	  *
	  * @param d drawable to change to bitmap
	  * Use ByteArrayOutputStream to get the bitmap into the stream
	  */ 
	 public InputStream drawableToInputStream(Drawable d) throws IOException {
	    BitmapDrawable bitDw = ((BitmapDrawable) d);
	    Bitmap bitmap = bitDw.getBitmap();
	    return bitmapByteArrayOutputStream(bitmap);
	 }
	 
	 /**
	  * Use ByteArrayOutputStream to get the bitmap into the stream
	  * 
	  * @param bitmap to read to input stream
	  */ 
	 public InputStream bitmapToInputStream(Bitmap bitmap) throws IOException {
	    return bitmapByteArrayOutputStream(bitmap);
	 }

	 /**
	  * Use ByteArrayOutputStream to get the bitmap into the stream
	  *
	  * @param bitmap to read stream from using ByteArrayOutputStream
	  * @return inputStream if successfully compressed to the specified stream.
	  */ 
	 public InputStream bitmapByteArrayOutputStream(Bitmap bitmap) {
	    ByteArrayOutputStream stream = new ByteArrayOutputStream();
	    bitmap.compress(compressFormat, 100, stream);
	    byte[] imageInByte = stream.toByteArray();
	    System.out.println("........length......"+imageInByte);
	    ByteArrayInputStream bis = new ByteArrayInputStream(imageInByte);
	    return bis;
	 }
	 
	 public Bitmap getResourceAsBitmap(LoaderOptions options,int resId) throws IOException {
		File f = options.getDiscCache().getFile(String.valueOf(resId));
		Bitmap bitmap = BitmapDecoder.decodeFile(f);

		if (bitmap != null) {
			return bitmap;
		}
		bitmap = BitmapFactory.decodeResource(context.getResources(), resId);
		options.getMemoryCache().put(String.valueOf(resId), bitmap);
		return bitmap;
	 }
}
