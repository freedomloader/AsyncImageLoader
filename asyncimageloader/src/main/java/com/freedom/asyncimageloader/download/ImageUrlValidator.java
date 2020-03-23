package com.freedom.asyncimageloader.download;

import com.freedom.asyncimageloader.uri.*;
import com.freedom.asyncimageloader.uri.URLEncoded.UriScheme;
import org.apache.http.conn.scheme.*;
import java.util.*;

public class ImageUrlValidator implements ImageValidator {

	public ImageUrlValidator() {

	}

	public ImgValidator websiteMatch(String cweb, List<String> websites) {
		ImgValidator validator = new ImgValidator();
		UriScheme scheme = UriScheme.match(UriSubmake.subUriQueryFormat(cweb));
		URLEncoded encoded = null;

		switch (scheme) {
			case HTTP: case HTTPS:
				validator.isNetworkUrl = true;
				encoded = URLEncoded.parse(cweb);
				String host = encoded.getHost();
				String authority = encoded.getAuthority();

				validator.extenstion = host;
				for (String website : websites) {
					URLEncoded checkencoded = URLEncoded.parse(website);
					String checkhost = checkencoded.getHost();
					String checkauthority = checkencoded.getAuthority();

					if (host.equals(checkhost)) {
						validator.isValid = true;
					}
					else if (authority.equals(checkauthority)) {
						validator.isValid = true;
					}
					if (validator.isValid)
						return validator;
				}
				break;
			default:
				break;
		}
		return validator;
	}

	public ImgValidator isValidImageUrl(String url) {
		ImgValidator validator = new ImgValidator();
		UriScheme scheme = UriScheme.match(UriSubmake.subUriQueryFormat(url));
		URLEncoded encoded = null;
		String extention = "";

		switch (scheme) {
			case HTTP: case HTTPS:
				validator.isNetworkUrl = true;
				encoded = URLEncoded.parse(url);

				if (encoded.hasQuery()) {
					encoded.removeQueryFromUrl();
					extention = encoded.getExtension();
				} else {
					extention = encoded.getExtension();
				}
				validator.extenstion = extention;
				for (String ext : ValidImageProvider()) {
					if (ext.equals(extention)) {
						validator.isValid = true;
						return validator;
					}
				}
				break;
			case FILE: case THUMB:
				validator.isNetworkUrl = false;
				encoded = URLEncoded.parse(url);

				if (encoded.hasQuery()) {
					encoded.removeQueryFromUrl();
					extention = encoded.getExtension();
				} else {
					extention = encoded.getExtension();
				}
				validator.extenstion = extention;
				for (String ext : ValidImageProvider()) {
					if (ext.equals(extention)) {
						validator.isValid = true;
						return validator;
					}
				}
				break;
			default:
				break;
		}
		return validator;
	}

	public String[] ValidImageProvider() {
		return new String[] {
				"jpg", "gif", "png", "bmp",
				"JPG", "GIF","PNG", "BMP",
				"JpG", "GiF","PnG", "BmP",
				".jpg", ".gif",".png", ".bmp",
				"..jpg", "..gif","..png", "..bmp",
				".JPG", ".GIF",".PNG", ".BMP",
				".JpG", ".GiF",".PnG", ".BmP",
				"jpg.jpg", "gif.gif","png.png", "bmp.bmp"
				,".mp4","mp4",".3gp","3gp",".avi","avi"
		};
	}
}