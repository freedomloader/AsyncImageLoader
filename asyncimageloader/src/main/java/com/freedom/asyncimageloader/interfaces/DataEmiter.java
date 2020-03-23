package com.freedom.asyncimageloader.interfaces;
import com.freedom.asyncimageloader.*;
import android.graphics.*;
import com.freedom.asyncimageloader.assist.*;

public class DataEmiter {
	  public String uri;
	  public PhotoLoadFrom loadedFrom;
	  public String infoValue;
	  public Bitmap bitmap;
	  public Resizer targetSize;
	  public long startTime;
	  
	  public DataEmiter() { }
	  
	  @Override
	  public String toString() {
		  StringWriter stringAppend = new StringWriter()
		  .append(" uri: ",uri)
		  .append(" loaded from: ",loadedFrom.toString())
		  .append(" info: ",infoValue)
		  .append(" start time: ",startTime)
		  .append(" bitmap: ",bitmap != null)
		  .flush();
		  return stringAppend.toString();
	 }
   }
