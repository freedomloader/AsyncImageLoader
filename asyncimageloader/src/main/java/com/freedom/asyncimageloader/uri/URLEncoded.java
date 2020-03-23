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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import com.freedom.asyncimageloader.StringWriter;

import android.net.Uri;

public class URLEncoded {
	private final static String RAND_NAME = "&";
	private final static String SEMI_NAME = ";";
	final static String MAKE_NAME = "?";

	public URLEncoded() {}

	public URLEncoded setQueryMap(Map<String, List<String>> uriQueryMap) {
		URLEncoded queryString = new URLEncoded();
		for (Map.Entry<String, List<String>> entry : uriQueryMap.entrySet()) {
			queryString.uriQueryMap.put(entry.getKey(),new ArrayList<String>(entry.getValue()));
		}
		return queryString;
	}

	/**
	 * Set charsetName.
	 * <p>
	 * @param charset charsetName
	 * @return self
	 */
	public URLEncoded setCharName(String charset) {
		CHARSET_NAME = charset;
		return this;
	}

	/**
	 * create URL and add the query of the URL.
	 * <p>
	 * @param uri URL string to add to map
	 * @return self
	 */
	public static URLEncoded parse(final URI uri) {
		return parse(uri.toString());
	}

	/**
	 * create URL and add the query of the URL.
	 * <p>
	 * @param uri URL string to add to map
	 * @return self
	 */
	public static URLEncoded parse(final Uri uri) {
		return parse(uri.toString());
	}

	/**
	 * create URL and add the query of the URL.
	 * <p>
	 * @param url giving URL
	 *  query parameter string to add
	 * @return self
	 */
	public static URLEncoded parse(final CharSequence url) {
		URLEncoded encodeUrl = new URLEncoded();
		//remove query if any
		String surl = encodeUrl.removeQuery(url.toString());
		encodeUrl.url = surl;

		try {
			//Retrieve query if any
			encodeUrl.doQueryWork(retrieveQuery(url.toString()), UseType.APPEND);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return encodeUrl;
	}

	/**
	 * Remove parameter, scheme, from URL and return URL domain
	 *
	 * @param url given URL
	 * @return new URL
	 */
	public static String stripQueryStringAndHashFromPath(String url) {
		return url.replaceAll(("(\\?.*|\\#.*)"), "");
	}

	/**
	 * Encode URL
	 *
	 * @param url given URL to encoded
	 * @return encodedUrl
	 */
	public static String encodeUri(String url) {
		String encodedUrl = Uri.encode(url, "@#&=*+-_.,:!?()/~'%");
		return encodedUrl;
	}

	/**
	 * Check if URL has parameters
	 *
	 * @see #isEmpty()
	 * url given URL
	 * @return
	 */
	public boolean hasQuery() {
		return !isEmpty() ? true : false;
	}

	/**
	 * Find the giving key inside query parameter if exits Returns {@code value}
	 * else if it does not exist, Returns {@code false}
	 * <p>
	 *
	 * @see #get(String)
	 * @param key key of the parameter to be find
	 * @return query in ("Boolean",true,false)
	 */
	public boolean getBoolean(final String key) {
		String queryBoolean = get(key);
		return queryBoolean.equals("true");
	}

	/**
	 * Find the giving key inside query parameter if exits Returns {@code value}
	 * else if it does not exist, Returns {@code 0}
	 * <p>
	 *
	 * @see #get(String)
	 * @param key key of the parameter to be find
	 * @return query in ("Integer",123456789)
	 */
	public int getInt(final String key) {
		String queryBoolean = get(key);
		return Integer.valueOf(queryBoolean);
	}

	/**
	 * Find the giving key inside query parameter if exits Returns {@code value}
	 * else if it does not exist, Returns {@code null}
	 * <p>
	 * if only single value of the parameter with giving key find then return value of the key
	 *
	 * You can also validate if key is find using {contains(key)}
	 * <p>
	 *
	 * @param key key of the parameter to be find
	 * @return <tt>true</tt> - return the value in key; <tt>false</tt> - return null
	 *        if key is not find in query
	 */
	public String get(final String key) {
		List<String> value = getQueryParameters(key);
		if (value == null || value.isEmpty()) {
			return null;
		}
		return value.get(0);
	}

	/**
	 * Sets a new query parameter {#add(key, value,type)}.
	 * <p>
	 * if such parameters with new key already exist in query parameter, then remove it and then
	 * add new to the query parameters with new value;  - else if not exits then create new one.
	 *
	 * @param key key of the query parameter
	 * @param value value of the query parameter.
	 * @return self
	 */
	public URLEncoded add(final String key, final String value) {
		doValues(key,value,UseType.ADD);
		return this;
	}

	/**
	 * Sets a new query parameter {#add(key, value,type)}.
	 * <p>
	 * if such parameters with new key already exist in query parameter, then remove it and then
	 * add new to the query parameters with new value; - else if not exits then create new one.
	 *
	 * <p>This can only be use when using {@code integer}, {#add("key",323323223)}:
	 * </p>
	 *
	 * @param key key of the query parameter
	 * @param value value of the query parameter.
	 * @return self
	 */
	public URLEncoded add(final String key, final int value) {
		doValues(key,String.valueOf(value),UseType.ADD);
		return this;
	}

	/**
	 * Sets a new query parameter {#add(key, value)}.
	 *
	 * @param key key of the query parameter
	 * @param value value of the query parameter.
	 * @return self
	 */
	public URLEncoded add(String key, long value) {
		if (key != null) {
			doValues(key,String.valueOf(value),UseType.ADD);
		}
		return this;
	}

	/**
	 * Sets a new query parameter {#add(query)}.
	 * <p>
	 * if such parameters with new key already exist in query parameter, then remove it and then
	 * add new to the query parameters with new value;
	 *
	 * @param query query to add to parameter.
	 * @return self
	 */
	public URLEncoded add(final String query) {
		doQueryWork(query, UseType.ADD);
		return this;
	}

	/**
	 * Sets a new query parameter {#add(key, value,type)}.
	 * <p>
	 * if such parameters with new key already exist in query parameter, then remove it and then
	 * add new to the query parameters with new value;  - else if not exits then create new one.
	 *
	 * @param key key of the query parameter
	 * @param value value of the query parameter.
	 * @param type if request should append or add or reset or remove
	 * @return self
	 */
	public URLEncoded add(String key,String value,UseType type) {
		doValues(key,value,type);
		return this;
	}

	/**
	 * Sets fast method to append to query.
	 * <p>
	 * @param uri given URL
	 * @param key key of the query parameter
	 * @param value value of the query parameter.
	 * @return query parameters
	 */
	public String appendToExitingQuery(String uri, String key,String value) {
		return  uri.indexOf('=') > 0 ? uri+"&" : uri+"?" + key + "=" + value;
	}

	/**
	 * append a new query with string value example{@code key=value} integer.
	 *  <p>
	 * if such parameters with new key already exist in map query, then remove it and then
	 * add new to the query parameters with new value;  - else if not exits then create new one.
	 *
	 * <p>This can only be use when using {@code integer}, {#add("key",323323223)}:
	 * </p>
	 *
	 * @param key {@code key} of the query parameter
	 * @param value {@code value} of the query parameter in number or integer
	 * @return self
	 */
	public URLEncoded append(final String key, final int value) {
		return append(key,String.valueOf(value));
	}

	/**
	 * append a new query with string value example{@code key=value}.
	 * <p>
	 * if such parameters with new key already exist in query parameter, then remove it and then
	 * add new to the query parameters with new value;  - else if not exits then create new one.
	 *
	 * @param key {@code key} of the query parameter
	 * @param value {@code value} of the query parameter
	 * @return self
	 */
	public URLEncoded append(final String key, final String value ) {
		doValues(key,value,UseType.APPEND);
		return this;
	}

	/**
	 * append query with string value {@code key=value}.
	 * <p>
	 * if such parameters with new key already exist in query parameter, then remove it and then
	 * add new to the query parameters with new value;  - else if not exits then create new one.
	 *
	 * @param query query parameter
	 * @return self
	 */
	public URLEncoded append(final String query) {
		doQueryWork(query, UseType.APPEND);
		return this;
	}

	/**
	 * Check if query is empty.
	 *
	 * if true mean query has parameters with value and key
	 * <p>This method can only be call only when request URI with {parse}.</p>
	 *
	 * @return <tt>true</tt> - if the query has no parameters; <tt>false</tt> - query has no parameters
	 */
	public boolean isEmpty() {
		return uriQueryMap.isEmpty();
	}

	/**
	 * Removes the key from query map.
	 * <p>
	 * check if have multiple key if true remove it all.
	 *
	 * @param key key to remove form query map
	 * @return self
	 */
	public URLEncoded remove(final String key) {
		doValues(key,null,UseType.REMOVE);
		return this;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}

		if (!(obj instanceof URLEncoded)) {
			return false;
		}

		String urlQuery = toString();
		String encodeUrl = ((URLEncoded) obj).toString();

		return urlQuery.equals(encodeUrl);
	}

	/**
	 * Get string query and return in hash for query parameters
	 * @return hash code of value for this query parameter.
	 */
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	/**
	 * Returns query parameters.
	 *
	 * @return URI with query parameters
	 *        in query map use method (url.indexOf('=') > 0 ? "&" : "?";)
	 *        to get the correct URL with query
	 */
	public String getQuery() {
		StringBuilder builder = getQuery(QuerySeparators.RAND,"&");
		return builder.toString();
	}

	/**
	 * Returns URL.
	 *
	 * @param value value use to get URL
	 * @return decoded URL
	 */
	public String getURL(URIValue value) {
		return doRealQueryWork(url,value);
	}

	/**
	 * Returns URL.
	 *
	 * @return URL
	 */
	public String getURL() {
		return url;
	}

	/**
	 * Returns URL.
	 *
	 * @return URL
	 */
	public String getUrl() {
		StringBuilder builder = new StringBuilder();

		if(url != null) {
			builder.append(url);
		}
		return builder.toString();
	}

	/**
	 * check Encoded URL if is a local URL.
	 *
	 * @return <tt>true</tt> - if URL is local; <tt>false</tt> - URL contain network
	 */
	public boolean isLocalUri() {
		switch (getSchemeType()) {
			case HTTP: case HTTPS:
				return false;
			default:
				return true;
		}
	}

	/**
	 * print out URL.
	 *
	 * @return {com.freedom.StringWriter.StringWriter StringWriter}
	 */
	public String printOut() {
		StringWriter writer = null;
		try {
			writer = new StringWriter()
					.append("===============START OR URL ===============")
					.append(" path: "+ getPath())
					.append(" scheme: "+getScheme())
					.append(" no scheme: "+removeScheme())
					.append(" authority: "+ getAuthority())
					.append("===============END OF URL ===============").flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return writer.toString();
	}

	/**
	 * Returns the keys with value.
	 *
	 * @return String
	 */
	public String printOutAll() {
		StringWriter writer = new StringWriter();
		Map<String, List<String>> mapNames = getURLParameter();

		for (String key : mapNames.keySet()) {
			List<String> valueNames = mapNames.get(key);
			if (valueNames != null && !valueNames.isEmpty()) {
				writer.append(key+": "+valueNames.get(0));
			}
		}
		return writer.flush().toString();
	}

	/**
	 * Returns the URI string of the query parameters.
	 *
	 * @return UriQuery
	 */
	@Override
	public String toString() {
		return toString(QuerySeparators.RAND,null);
	}

	/**
	 * Returns the URI string of the query parameters.
	 *
	 * @param other other string to be use for separator
	 * @return UriQuery
	 */
	public String toString(String other) {
		return toString(QuerySeparators.OTHER,other);
	}

	/**
	 * Returns the URI string of the query parameters.
	 *
	 * @param type separator to be use between uri parameters
	 * @return UriQuery
	 */
	public String toString(QuerySeparators type) {
		return toString(type,MAKE_NAME);
	}

	/**
	 * Returns the URI string of the query parameters.
	 *
	 * @param type separator to be use between URL parameters
	 * @param other other string to be use for separator
	 *
	 * @return UriQuery
	 */
	public String toString(QuerySeparators type,String other) {
		StringBuilder builder = getQuery(type,other);
		String query = builder.toString();
		String xx = query.equals("") ? "" : !query.startsWith("?") ? "?" : "";
		return getURL()+xx+query;
	}

	/**
	 * Returns the query parameters.
	 *
	 * @param type separator to be use between URL parameters
	 * @param other other string to be use for separator
	 *
	 * @return builder
	 */
	private StringBuilder getQuery(QuerySeparators type,String other) {
		StringBuilder builder = new StringBuilder();
		for(String keys : getQueryParameterKeys()) {
			for (String values : getQueryParameters(keys)) {
				if (builder.length() != 0) {
					builder.append(type == QuerySeparators.QUERY ? MAKE_NAME : type == QuerySeparators.SEMI ? SEMI_NAME :
							type == QuerySeparators.RAND ? RAND_NAME : type == QuerySeparators.OTHER ? other : RAND_NAME);
				}
				try {
					builder.append(encode(keys));

					if (values != null) {
						builder.append('=');
						builder.append(encode(values));
					}
				} catch (Exception e) {
					throw new IllegalStateException(e.getMessage());
				}
			}
		}
		return builder;
	}

	private boolean doValues(final String key, final String value, final UseType type) {
		if (key == null) {
			throw new NullPointerException("name should not be null");
		}
		switch (type) {
			case APPEND:
				return appendValue(key,value);
			case ADD:
				return appendValue(key,value);
			case REMOVE:
				removeKey(key);
			default:
				return false;
		}
	}

	/**
	 * Returns a List of Keys
	 * <p>
	 * @return a List of String containing the parameters keys, else return  {@code null} if
	 *         the query empty
	 */
	private Set<String> getQueryParameterKeys() {
		return this.uriQueryMap.keySet();
	}

	/**
	 * Clear the query parameter.
	 * <p>
	 *
	 * @return true
	 */
	private boolean clearQuery() {
		this.uriQueryMap.clear();
		return true;
	}

	private Set<Entry<String, List<String>>> entrySet() {
		return this.uriQueryMap.entrySet();
	}

	/**
	 * Returns true if key is find in query map.
	 * <p>
	 * @return <tt>true</tt> - if key is find; <tt>false</tt> - error occurred maybe key not find
	 */
	public boolean contains(final String key) {
		return this.uriQueryMap.containsKey(key);
	}

	/**
	 * Get multiple keys from query map
	 * return {@code null} if there are no parameters find in query map
	 * <p>
	 *
	 * @return <tt>true</tt> - List of each name of a parameter; <tt>false</tt> -
	 *       returns an empty Iterator cause keys is empty
	 */
	public Iterator<String> getQueryKeys() {
		return this.uriQueryMap.keySet().iterator();
	}

	/**
	 * Returns a List<String> of parameters return {@code null} if parameters null
	 * <p>
	 * If the parameter has a single value, the List has a size of 1.
	 *
	 * @param key key of parameter to get from query
	 * @return List<String> containing the parameter's values; {@code null}
	 *       if the parameters does not exist
	 */
	public List<String> getQueryParameters(final String key) {
		return this.uriQueryMap.get(key);
	}

	/**
	 * do work with query parameters
	 * <p>
	 *
	 * @param parameters query to be decoded with request object
	 * @param type if request should append or add or reset or remove
	 */
	private void doQueryWork(final CharSequence parameters, UseType type) {
		if (parameters == null || parameters.length() == 0)
			return;

		Set<String> setQuery = null;
		StringTokenizer tokenss = StringToke(parameters.toString());

		while (tokenss.hasMoreTokens()) {
			String token = tokenss.nextToken();
			try {
				String key = valuesQuery(token,true);
				String value = valuesQuery(token,false);

				switch (type) {
					case ADD:
						if (setQuery == null) {
							setQuery = new HashSet<String>();
						}
						if (!setQuery.contains(key)) {
							remove(key);
						}
						setQuery.add(key);
						break;
					case REMOVE:
						removeKey(key);
						break;
					case RESET:
						int index = value.indexOf("?");
						if (index > 0) {
							key = decode(key.substring(index + 10,key.length() - 1));
						}
						break;
					case APPEND:
						break;
				}
				doValues(key,value,UseType.APPEND);
			} catch (Exception e ) {
				throw new IllegalStateException(e.getMessage());
			}
		}
	}

	/**
	 * append if parameters with this key already exist, then it will be remove
	 * and new one will be created
	 * <p>
	 *
	 * @param key query to be decoded with request object
	 * @param value if request should append or add or reset or remove
	 */
	private boolean appendValue(final String key, final String value) {
		List<String> valueKeys = getQueryParameters(key);
		if (valueKeys != null) {
			removeKey(key);
		}
		//name not exits create one
		List<String> nqueryValues = new ArrayList<String>();
		nqueryValues.add(value);
		this.uriQueryMap.put(key,nqueryValues);
		return true;
	}

	/**
	 * Removes the key from query map.
	 * <p>
	 * check if have multiple key if true remove it all.
	 *
	 * @param key key to remove form query map
	 * @return <tt>true</tt> - if key successful remove; <tt>false</tt> - error occurred maybe key not find
	 */
	private boolean removeKey(String key) {
		return this.uriQueryMap.remove(key) != null;
	}

	private StringTokenizer StringToke(String value) {
		return new StringTokenizer(value,String.valueOf(RAND_NAME) + SEMI_NAME);
	}

	/**
	 * do real work with query parameters
	 * <p>
	 *
	 * @param curl given URL
	 * @param type type should do work only for type request
	 * @return
	 */
	private String doRealQueryWork(String curl,URIValue type) {
		int indexOf = curl.indexOf("/");
		int index = curl.indexOf("?");
		String url = curl;
		try {
			if (indexOf > 0) {
				switch (type) {
					case GET_AUTHORITY:
						url = getAuthority();
						break;
					case GET_BEFORE_LAST_PATH_OF_URL:
						url = getPathBeforeLastPath();
						break;
					case GET_AND_REMOVE_ALL_QUERY:
						url = removeQuery(url);
						break;
					case GET_LAST_PATH:
						url = getLastPath();
						break;
					case GET_PATH:
						url = getPath();
						break;
					case GET_HOST:
						url = getHost();
						break;
					case GET_SCHEME:
						url = getScheme();
						break;
				}
			} else {
				if (index > 0) {
					url = decode(url.substring(0, index));
				} else {
					url = decode(url.substring(0,-index + -indexOf));
				}
			}
		} catch (Exception e) {
			//throw new IllegalStateException(e.getMessage());
		}
		return url;
	}

	/**
	 * Get value and key in URL if {@code isKey} true get only key
	 * else get value from URL
	 *
	 * @param url given URL
	 * @param iskey get only key
	 * @return
	 */
	private String valuesQuery(String url,boolean iskey){
		int index = url.indexOf('=');
		if (index == -1) {
			if(iskey) {
				return decode(url);
			}else {
				return null;
			}
		} else {
			if(iskey) {
				return decode(url.substring(0, index));
			}else {
				return decode(url.substring(index + 1));
			}
		}
	}

	/**
	 * Get List of query parameters
	 * {java.util.List<String> String}
	 *
	 * @return {java.util.LinkedHashMap<String, List<String>> LinkedHashMap}
	 */
	public Map<String, List<String>> getURLParameter() {
		LinkedHashMap<String, List<String>> querymap = new LinkedHashMap<String, List<String>>();
		for (Map.Entry<String, List<String>> entry : entrySet()) {
			List<String> listValues = entry.getValue();
			querymap.put(entry.getKey(),new ArrayList<String>(listValues));
		}
		return querymap;
	}

	/**
	 * Get base domain name of URL. E.g. http://www.google.com/support/mobile/ will return
	 * google.com
	 *
	 * @see #getHost()
	 *  URL to get domain from
	 * @return domain name from URL
	 */
	public String getAuthority() {
		String authority = getHost();
		//authority = authority != null ? authority : "";

		int sIndex = 0;
		int nIndex = authority.indexOf(".");
		int lIndex = authority.lastIndexOf(".");
		//check if many nextIndex remove them
		while (nIndex < lIndex) {
			sIndex = nIndex + 1;
			//try to check if indexOf
			nIndex = authority.indexOf(".", sIndex);
		}
		authority = sIndex > 0 ? authority.substring(sIndex) : authority;
		return authority;
	}

	/**
	 * Get Last Extension of URL. E.g. http://www.domain.com/search.php will return
	 * {@code php}
	 *  url URL to get last extension from
	 * @return Extension
	 */
	public String getExtension() {
		String extension = getPath();
		int gIndex = 0;
		int nIndex = extension.indexOf(".");
		int lIndex = extension.lastIndexOf(".");

		while (nIndex < lIndex) {
			gIndex = nIndex + 1;
			nIndex = extension.indexOf(".", gIndex);
		}
		extension = gIndex > 0 ? decode(extension.substring(gIndex)) : extension.indexOf(".") > 0 ? extension : "";
		return extension.indexOf(".") > 0 ? decode(extension.substring(extension.indexOf(".")+ 1)) : extension;
	}

	/**
	 * Get Extension of URL. E.g. http://www.google.com will return
	 * {@code com}
	 *  URL to get extension from
	 * @return Extension
	 */
	public String getFirstExtension() {
		String extension = getAuthority();
		int startindex = extension.indexOf(".");
		if (startindex > 0) {
			extension = decode(extension.substring(startindex+ 1));
		}
		return extension;
	}

	/**
	 * Get only scheme part("scheme://") type
	 *
	 * @see #getScheme()
	 * @return {com.freedom.URL.URLEncoded.UriScheme UriScheme}
	 */
	public UriScheme getSchemeType() {
		return UriScheme.match(url);
	}

	/**
	 * Get only scheme part("scheme://") of URL. E.g. http://www.google.com will return
	 * {@code ("http")}
	 *
	 * @return scheme
	 */
	public String getScheme() {
		int indexOf = url.indexOf(":");
		String scheme = indexOf > 0 ? decode(url.substring(0, indexOf)) : "";
		return scheme;
	}

	/**
	 * Get only scheme specific part("//") of URL. E.g. http://www.google.com will return
	 * {@code ("//www.google.com")}
	 *
	 * @return scheme part("//")
	 */
	public String getSchemeSpecificPath() {
		int indexOf = url.indexOf("/");
		String schemeSPart = indexOf > 0 ? decode(url.substring(indexOf + 0)) : "";
		String xx = "?";
		return schemeSPart+xx+getQuery();
	}

	/**
	 * Get only the part of URL. E.g. http://www.google.com/support/mobile/ will return
	 * {@code path: /support/mobile/}
	 *
	 * @return {@code path}
	 */
	public String getPath() {
		String path = removeQuery(removeScheme());
		int index = path.indexOf("/");
		if (index > 0) {
			path = decode(path.substring(index + 0));
		} else {
			//check if next index is low than last index if true make path null
			//else if next index is more than last index make path path
			path = path.indexOf(".") < path.lastIndexOf(".") ? "" : path;
		}
		return path;
	}

	private String getSlashPath() {
		String path = getPath();
		return !path.equals("") && !path.startsWith("/") ? "/"+path : path;
	}

	public List<String> getPathSegments() {
		String pathList = getSlashPath();
		List<String> paths = new ArrayList<String>();
		int gIndex = 0;
		int nIndex = pathList.indexOf("/");
		String[] parts = pathList.split("/");
		int lIndex = pathList.lastIndexOf("/");
		int sIndex = parts.length;

		while (nIndex < lIndex) {
			gIndex = nIndex + 1;
			sIndex = sIndex- 1;
			paths.add(decode(parts[parts.length - sIndex]));
			nIndex = pathList.indexOf("/", gIndex);
		}
		return paths;
	}

	/**
	 * Get Before Last Part of URL. E.g. http://www.google.com/support/mobile/ will return
	 * {@code support}
	 *
	 * @return {@code path}
	 */
	public String getPathBeforeLastPath() {
		String beforeLast = getSlashPath();
		String[] parts = beforeLast.split("/");
		beforeLast = parts.length > 1 ? decode(parts[parts.length - 2]) : "";
		return beforeLast;
	}

	/**
	 * Get Last path of URL. E.g. http://www.google.com/support/mobile/ will return
	 * {@code mobile}
	 *
	 * @return {@code path}
	 */
	public String getLastPath() {
		String[] parts = getSlashPath().split("/");
		String lastPart = parts.length > 0 ? decode(parts[parts.length - 1]) : "";
		return lastPart;
	}

	/**
	 * Get Last path of URL without extension. E.g. http://domain.com/search.php will return
	 * {@code search}
	 *
	 * @return {@code path}
	 */
	public String getLastPathWithoutExtension() {
		String url = getLastPath();
		int indexOf = url.indexOf(".");
		url = indexOf > 0 ? decode(url.substring(0, indexOf)) : "";
		return url;
	}

	public String getPathWithoutQuery() {
		// TODO: Implement this method
		return "";
	}

	public String getSlashPathWithoutQuery() {
		// TODO: Implement this method
		return "";
	}

	public String getPathWithoutLastPath() {
		// TODO: Implement this method
		return "";
	}

	/**
	 * Remove only scheme part("scheme://") from URL. this finally remove scheme from URL
	 *
	 * <p>This method can only be use when URLMaster is still valid.</p>
	 * If you try to get scheme from URL with same URIMaster it will return null
	 *
	 * @see #removeScheme()
	 */
	public void removeSchemeFromUrl() {
		this.url = removeScheme();
	}

	/**
	 * Remove all query from URL. this finally remove query from URL
	 *
	 * <p>This method can only be use when URLMaster is still valid.</p>
	 * If you try to get query from URL with same URIMaster it will return only the URL
	 *
	 * @see #clearQuery()
	 * @see #removeQuery()
	 */
	public void removeQueryFromUrl() {
		clearQuery();
	}

	/**
	 * Remove only scheme part("scheme://") from URL. E.g. http://www.google.com will return
	 * google.com
	 *
	 * @return URL
	 */
	public String removeScheme() {
		int index = url.indexOf("//");
		String url = this.url;
		if (index > 0) {
			url = decode(url.substring(index + 2));
		} else {
			url = decode(url);
		}
		return url;
	}

	/**
	 * Remove all slash part("/good/") from URL. E.g. "/good/" will return
	 * "good"
	 *
	 * @return URL
	 */
	public String removeAllSlash() {
		this.url = this.url.replaceAll("\\/","");
		return this.url;
	}

	/**
	 * Remove all query from URL. E.g. http://www.google.com/search?q=lovely will return
	 * http://www.google.com/search
	 *
	 * @return URL
	 */
	public String removeQuery() {
		int index = url.indexOf("?");
		String url = this.url;
		if (index > 0) {
			url = decode(url.substring(0, index));
		} else {
			index = url.lastIndexOf("/");
			url = decode(url.substring(0, index));
		}
		return url;
	}

	/**
	 * Change the given URL such as http://www.stackoverflow.com and return
	 * www.stackoverflow.com
	 *
	 * @return domain URL
	 */
	public String getHost(){
		if(url == null || url.length() == 0)
			return "";
		int slash = url.indexOf("//");
		if(slash == -1)
			slash = 0;
		else
			slash += 2;
		int end = url.indexOf('/', slash);
		end = end >= 0 ? end : url.length();
		int port = url.indexOf(':', slash);
		end = (port > 0 && port < end) ? port : end;
		String host = url.substring(slash, end);
		return host.indexOf('.') > 0 ? host : "";
	}

	/**
	 * Get all query from URL. E.g. http://www.google.com/search?q=lovely will return
	 * q=lovely
	 *
	 * @param url given URL
	 * @return query
	 */
	private static String retrieveQuery(String url) {
		int index = url.indexOf("?");
		String query;
		if (index > 0) {
			query = decode(url.substring(index + 1));
		} else {
			query = null;
		}
		return query;
	}

	/**
	 * Encode URL value
	 *
	 * @param value
	 * @return encoded value
	 */
	public static String encode(String value) {
		try {
			return URLEncoder.encode(value, CHARSET_NAME);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return value;
	}

	/**
	 * Decode URL value
	 *
	 * @param value
	 * @return decoded value
	 */
	public static String decode(String value) {
		try {
			return URLDecoder.decode(value, CHARSET_NAME);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return value;
	}

	private String removeQuery(String url) {
		int index = url.indexOf("?");
		url = index > 0 ? decode(url.substring(0, index)) : url;
		return url;
	}

	private final Map<String, List<String>>	uriQueryMap = new LinkedHashMap<String, List<String>>();
	private String url;
	private static String CHARSET_NAME = "UTF-8";

	public enum QuerySeparators {
		QUERY,
		SEMI,
		RAND,
		OTHER
	};

	enum QueryType {
		SINGLE,
		MULTIPLE
	};

	enum UseType {
		ADD,
		REMOVE,
		APPEND,
		RESET
	};

	public enum URIValue {
		GET_AUTHORITY,
		GET_BEFORE_LAST_PATH_OF_URL,
		GET_AND_REMOVE_ALL_QUERY,
		GET_LAST_PATH,
		GET_PATH,
		GET_HOST,
		GET_SCHEME,
	};

	public interface SchemeCallback {
		void onSchemeFind(UriScheme scheme,String schemeString);
		void onException(Throwable e);
	};

	/** This is custom scheme of URI. Allow many methods of schemes for URL. */
	public enum UriScheme {
		/** Network scheme. */
		HTTP("http"), HTTPS("https"),
		/** Content and raw scheme. */
		CONTENT("content"), RAW("raw"),
		/** Drawable and assets scheme. */
		DRAWABLE("drawable"),ASSETS("assets"),
		/** File and thumb scheme. */
		FILE("file"),FILE_S("/storage/"),THUMB("thumb"), UNKNOWN_URI("");

		public String schemeType = null;
		private String type;
		private String withPrefix = "://";

		/** Place prefix on incoming scheme of URL. */
		private UriScheme(String uriType) {
			this.type = uriType;
			this.schemeType = type + withPrefix;
		}

		/**
		 * Get Scheme from given URL
		 * Get list of scheme value in {com.freedom.URL.URLEncoded.UriScheme UriScheme}
		 *
		 * If URL scheme not find return {com.freedom.URL.URLEncoded.UriScheme.UNKNOWN_URI UNKNOWN_URI}
		 *
		 * @param uri URL which contains define scheme
		 * @return URLScheme
		 */
		public static UriScheme match(String uri) {
			if (uri != null) {
				if (uri.startsWith(FILE_S.type))
					return FILE_S;

				for (UriScheme usm : values()) {
					if (uri.startsWith(usm.schemeType)) {
						return usm;
					}
				}
			}
			return UNKNOWN_URI;
		}

		/**
		 * Get Scheme from given URL and return scheme using callback
		 * @see #match(String)
		 *
		 * @param uri URL which contains define scheme
		 * @param callback which will be use to return URL scheme
		 */
		public static void match(String uri,SchemeCallback callback) {
			UriScheme scheme = match(uri);
			if(scheme != UNKNOWN_URI) {
				callback.onSchemeFind(scheme,scheme.toString());
				return ;
			}
			callback.onException(new Exception("URI scheme not find"));
		}

		/**
		 * Drag scheme part("scheme://") to URL. E.g. www.google.com will return
		 * http://www.google.com
		 *
		 * @param uri URL which scheme will be added to
		 * @return URLScheme
		 */
		public String drag(String uri) {
			return schemeType + uri;
		}

		/**
		 * Drag scheme part("scheme://") to URL. using the given scheme check given scheme if end with slash because
		 * given scheme might only contain E.g. ("http") instead of ("http://")
		 *
		 * @param scheme scheme given scheme to add to URL
		 * @param uri URL which scheme will be added to
		 * @return URLScheme
		 */
		public String drag(String scheme,String uri) {
			if(!scheme.endsWith("/")) {
				//URI only contain scheme with no prefix
				scheme += withPrefix;
			}
			return scheme + uri;
		}

		/**
		 * Remove only scheme part("scheme://") from URL E.g. http://www.google.com will return
		 * www.google.com
		 *
		 * If current scheme does not match URL scheme
		 * return URL
		 *
		 * @param uri URL which current scheme will be removed from
		 * @return URL
		 */
		public String remove(String uri) {
			if (uri == null) {
				//URI null return URI
				return uri;
			}
			if (!uri.startsWith(schemeType)) {
				//URI does not start with current scheme return URI
				return uri;
			}
			return uri.substring(schemeType.length());
		}

		/**
		 * Get scheme string using {toString()}
		 *
		 * <p>This method can only be call only when you sure you have valid {UriScheme}.
		 *  - else this might throw IllegalStateException
		 * </p>
		 * @return Scheme string
		 */
		@Override
		public String toString() {
			switch (this) {
				case HTTP:
					return "http";
				case HTTPS:
					return "https";
				case FILE:
					return "file";
				case FILE_S:
					return "/storage/";
				case CONTENT:
					return "content";
				case ASSETS:
					return "assets";
				case RAW:
					return "raw";
				case THUMB:
					return "thumb";
				case DRAWABLE:
					return "drawable";
				case UNKNOWN_URI:
					return "unknown uri";
				default:
					throw new IllegalStateException("Unknown uri: ");
			}
		}
	}
}

