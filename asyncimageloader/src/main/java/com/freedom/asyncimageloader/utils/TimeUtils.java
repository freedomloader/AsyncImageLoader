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
package com.freedom.asyncimageloader.utils;

import java.util.Calendar;
import java.util.Date;
 
public class TimeUtils {
    public static final int CACHE_DURATION_FOREVER = Integer.MAX_VALUE;
    public static final int DURATION_FOREVER = CACHE_DURATION_FOREVER;
	private static final int SECOND_MILLIS = 1000;
	private static final int CACHE_MILLS = SECOND_MILLIS * 60 * 60 * 24;

	private static final int MINUTE_MILLIS = 60 *SECOND_MILLIS;
	private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
	private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    private static final String AGO = " ago";
    private static final String DAYS = " days";
    private static final String A_MINUTE_AGO = "a minute";
    private static final String SECOND_AGO = " seconds";
    private static final String MINUTE_AGO = " minute";
    private static final String JUST_NOW = "just now";
    private static final String HOUR_AGO = "an hour";
    private static final String HOURS = " hours";;
    private static final String YESTERDAY = "yesterday";
    
    public static Date getCalendarTime() {
        Calendar cal = Calendar.getInstance();
		return cal.getTime();
    }
    
    public static String getAgoFor(long times) {
    	long time = times;
	    final long diff = getCalendarTime().getTime() - time;
		return getMillsAgo(diff,false,AGO);
    }
    
    public static String getAgoFor(long start,long end) {
    	long time = start;
	    final long diff = end - time;
		return getMillsAgo(diff,true,AGO);
    }
    
    public static String getAgoFor(long start,long end,String withAgo) {
    	long time = start;
	    final long diff = end - time;
		return getMillsAgo(diff,true,withAgo);
    }
    
    public static String getMillsAgo(final long diff) {
    	return getMillsAgo(diff,false,AGO);
    }
    
    public static String getMillsAgo(final long diff,boolean seconds,String withAgo) {
	    if (diff < MINUTE_MILLIS) {
	    	if (seconds) {
		        return diff / SECOND_MILLIS + SECOND_AGO + withAgo;
	    	} else {
		        return JUST_NOW;
	    	}
	    } else if (diff < 2 * MINUTE_MILLIS) {
	    	if (seconds) {
		        return diff / MINUTE_MILLIS + MINUTE_AGO + withAgo;
	    	} else {
		        return A_MINUTE_AGO + withAgo;
	    	}
	    } else if (diff < 50 * MINUTE_MILLIS) {
	        return diff / MINUTE_MILLIS + MINUTE_AGO + withAgo;
	    } else if (diff < 90 * MINUTE_MILLIS) {
	        return HOUR_AGO + withAgo;
	    } else if (diff < 24 * HOUR_MILLIS) {
	        return diff / HOUR_MILLIS + HOURS + withAgo;
	    } else if (diff < 48 * HOUR_MILLIS) {
	        return YESTERDAY;
	    } else {
	        return diff / DAY_MILLIS + DAYS + withAgo;
		}
	}
    
    public static class SECOND {
    	public static final int ONE_SECOND = 1 * SECOND_MILLIS;
    	public static final int TWO_SECOND = 2 * SECOND_MILLIS;
    	public static final int THREE_SECOND = 3 * SECOND_MILLIS;
    	public static final int FOUR_SECOND = 4 * SECOND_MILLIS;
    	public static final int FIVE_SECOND = 5 * SECOND_MILLIS;
    	public static final int TEN_SECOND = 10 * SECOND_MILLIS;
    }
    
    public static class MINUTE {
    	public static final int ONE_MINUTE = 1 * MINUTE_MILLIS;
    	public static final int TWO_MINUTE = 2 * MINUTE_MILLIS;
    	public static final int THREE_MINUTE = 3 * MINUTE_MILLIS;
    	public static final int FOUR_MINUTE = 4 * MINUTE_MILLIS;
    	public static final int FIVE_MINUTE = 5 * MINUTE_MILLIS;
    	public static final int TEN_MINUTE = 10 * MINUTE_MILLIS;
    }
    
    public static class DAY {
        public static final int ONE_DAYS = CACHE_MILLS * 1;
        public static final int TWO_DAYS = CACHE_MILLS * 2;
        public static final int THREE_DAYS = CACHE_MILLS * 3;
        public static final int FOUR_DAYS = CACHE_MILLS * 4;
        public static final int FIVE_DAYS = CACHE_MILLS * 5;
        public static final int SIX_DAYS = CACHE_MILLS * 6;
    }
    
    public static class HOUR {
    	public static final int ONE_HOUR = 59 * MINUTE_MILLIS;
    }
    
    public static class WEEK {
        public static final int ONE_WEEK = CACHE_MILLS * 7;
        public static final int TWO_WEEK = ONE_WEEK * 2;
        public static final int THREE_WEEK = ONE_WEEK * 3;
        public static final int FOUR_WEEK = ONE_WEEK * 4;
    }
    
    public static class MONTH {
        public static final int ONE_MONTH = 4 * CACHE_MILLS * 7;
    }
}