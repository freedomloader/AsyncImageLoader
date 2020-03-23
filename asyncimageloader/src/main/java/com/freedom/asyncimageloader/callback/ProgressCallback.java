package com.freedom.asyncimageloader.callback;

/** Callback for CustomImage events. */
	public interface ProgressCallback {
		/**
		 * update byte bitmap image download.
		 */
		void onProgressUpdate(int bytes, int total);
	}
