package com.freedom.asyncimageloader.assist;
import android.graphics.*;
import java.util.*;
import android.os.*;

public class BitmapMarge {
     private String uri;
     private List<Bitmap> bitmaps = null;
	 private Bitmap bitmap = null;
	 private boolean isSameView = false;
	 private boolean enableFullMarge = false;
	 private boolean disableUriBitmap = false;
	 private String margeKey = "";
	 private String keyUri = "";
	 Resizer margeResizer = null;
	 int margeLimit = 0;
	 private boolean enablePixelKey = false;
	 
	public BitmapMarge(List<Bitmap> bitmaps) {
		this.bitmaps = bitmaps;
	}
	
	public BitmapMarge(Bitmap bitmap) {
		this.bitmap = bitmap;
	}
	
	public BitmapMarge margeResizer(Resizer resizer) {
		this.margeResizer = resizer;
		return this;
	}
	
	public BitmapMarge margeLimit(int limit) {
		this.margeLimit = limit;
		enablefullMarge();
		return this;
	}
	
	public BitmapMarge margeKey(String key) {
		this.margeKey = key;
		return this;
	}
	
	public BitmapMarge disableUriBitmap() {
		this.disableUriBitmap = true;
		return this;
	}
	
	public BitmapMarge enablePixelKey(boolean pixelKey) {
		this.enablePixelKey = pixelKey;
		return this;
	}
	
	public BitmapMarge enableSameView(boolean isSameView) {
		this.isSameView = isSameView;
		return this;
	}
	
	public BitmapMarge enableSameView() {
		this.isSameView = true;
		return this;
	}
	
	public BitmapMarge enablefullMarge() {
		this.enableFullMarge = true;
		return this;
	}
	
	public boolean isDisableUriBitmap() {
		return disableUriBitmap;
	}
	
	public boolean isEnableFullMarge() {
		return enableFullMarge;
	}
	
	public boolean isSameView() {
		return isSameView;
	}
	
	public boolean isEnablePixelKey() {
		return enablePixelKey;
	}
	
	public String getMargeKey() {
		return margeKey;
	}
	
	public Resizer getMargeResizer() {
		return margeResizer;
	}
	
	public int getMargeLimit() {
		return margeLimit;
	}
	
	public List<Bitmap> getBitmaps() {
		return bitmaps;
	}
	
	public int getBitmapSize() {
		if (getBitmaps() != null) {
			return getBitmaps().size();
		} else {
			return 0;
		}
	}
	
	public Bitmap getBitmap() {
		return bitmap;
	}
	
	public void setKeyUri(String uri) {
		this.uri = uri;
	}
	
	public String getKey() {
		if (!keyUri.equals("")) {
			return keyUri;
		}
		int width = 0;
		int height = 0;
		if (getBitmap() != null) {
			width = getBitmap().getWidth();
			height = getBitmap().getHeight();
		}
		else if (getBitmaps() != null) {
			for (Bitmap b : getBitmaps()) {
				  width += b.getWidth();
				  height += b.getHeight();
			}
		}
		int count = getBitmaps() != null ? getBitmaps().size() : 0;
		StringBuffer builder = new StringBuffer();
		
		builder.append("count:").append(count);
		builder.append('\n');
		
		if (getMargeResizer() != null) {
			builder.append("margeResizer:").append(getMargeResizer().width).append('x').append(getMargeResizer().height);
			builder.append('\n');
		}
		builder.append("resize:").append(width).append('x').append(height);
		builder.append('\n');
		builder.append("key:").append(getMargeKey());
		builder.append('\n');
		
		//return "marge:key:"+getMargeKey()+" count:"+count+" size:"+width+"x"+height;
		return builder.toString();
	}
}
