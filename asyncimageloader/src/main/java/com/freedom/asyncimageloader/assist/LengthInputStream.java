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
package com.freedom.asyncimageloader.assist;

import java.io.IOException;
import java.io.InputStream;

public class LengthInputStream extends InputStream {

	private final InputStream stream;
	private final long fileLength;

	private long lenFile;

	public LengthInputStream(InputStream stream) throws IOException {
		this(stream, stream.available());
	}

	public LengthInputStream(InputStream stream, long length) {
		this.stream = stream;
		this.fileLength = length;
	}

	@Override
	public void close() throws IOException {
		stream.close();
	}

	@Override
	public void mark(final int readlimit) {
		lenFile = readlimit;
		stream.mark(readlimit);
	}

	@Override
	public synchronized int available() {
		return (int) (fileLength - lenFile);
	}

	@Override
	public int read() throws IOException {
		lenFile++;
		return stream.read();
	}

	@Override
	public int read(final byte[] buffer) throws IOException {
		return read(buffer, 0, buffer.length);
	}

	@Override
	public int read(final byte[] buffer, final int byteOffset,
			final int byteCount) throws IOException {
		lenFile += byteCount;
		return stream.read(buffer, byteOffset, byteCount);
	}

	@Override
	public long skip(final long byteCount) throws IOException {
		lenFile += byteCount;
		return stream.skip(byteCount);
	}

	@Override
	public synchronized void reset() throws IOException {
		lenFile = 0;
		stream.reset();
	}
}