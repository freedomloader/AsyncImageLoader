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
package com.freedom.asyncimageloader;

import java.io.PrintWriter;

public class StringWriter {

	public String msg;
	public String value;
	java.io.StringWriter logwriter = new java.io.StringWriter();
	private PrintWriter writer;

	public StringWriter() {
		logwriter = new java.io.StringWriter();
		writer = new PrintWriter(logwriter);
	}

	public StringWriter append(String msg) {
		writer.println(msg);
		return this;
	}

	public StringWriter append(String value, long msg) {
		return append(value, String.valueOf(msg));
	}

	public StringWriter append(String value, int msg) {
		return append(value, String.valueOf(msg));
	}

	public StringWriter append(String value, float msg) {
		return append(value, String.valueOf(msg));
	}

	public StringWriter append(String value, char msg) {
		return append(value, String.valueOf(msg));
	}

	public StringWriter append(String value, char[] msg) {
		return append(value, String.valueOf(msg));
	}

	public StringWriter append(String value, double msg) {
		return append(value, String.valueOf(msg));
	}

	public StringWriter append(String value, Object msg) {
		this.value = value;
		this.msg = String.valueOf(msg);
		writer.print(" " + value);
		writer.println(msg);
		return this;
	}

	public StringWriter append(String value, String msg) {
		this.value = value;
		this.msg = msg;
		writer.print(" " + value);
		writer.println(msg);
		return this;
	}

	public StringWriter flush() {
		writer.flush();
		return this;
	}

	@Override
	public String toString() {
		return logwriter.toString();
	}
}