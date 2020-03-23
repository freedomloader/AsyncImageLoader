package com.freedom.asyncimageloader.download;

import java.util.*;

public interface ImageValidator {
	public ImgValidator websiteMatch(String cweb,List<String> websites);
	public ImgValidator isValidImageUrl(String url);
	public String[] ValidImageProvider();
	
}
