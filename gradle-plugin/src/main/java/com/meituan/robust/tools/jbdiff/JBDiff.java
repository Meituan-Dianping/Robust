/*
 * Copyright (c) 2005, Joe Desbonnet, (jdesbonnet@gmail.com)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <copyright holder> ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <copyright holder> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.meituan.robust.tools.jbdiff;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Java Binary Diff utility. Based on bsdiff (v4.2) by Colin Percival (see
 * http://www.daemonology.net/bsdiff/ ) and distributed under BSD license.
 * 
 * <p>
 * Running this on large files will probably require an increase of the default
 * maximum heap size (use java -Xmx200m)
 * </p>
 * 
 * @author Joe Desbonnet, joe@galway.net
 * 
 */
public class JBDiff {

	// JBDiff extensions by Stefan.Liebig@compeople.de:
	//
	// - uses GZIP compressor to compress ALL of the blocks (ctrl,diff,extra).
	// - added interfaces that allows using of JBDiff with streams and byte
	// arrays.

//	private static final String VERSION = "jbdiff-0.1.0.1";

	// This is 'jbdiff40'.
	private static final byte[] MAGIC_BYTES = new byte[] { 0x6a, 0x62, 0x64,
			0x69, 0x66, 0x66, 0x34, 0x30 };

	private final static void split(int[] I, int[] V, int start, int len, int h) {

		int i, j, k, x, tmp, jj, kk;

		if (len < 16) {
			for (k = start; k < start + len; k += j) {
				j = 1;
				x = V[I[k] + h];
				for (i = 1; k + i < start + len; i++) {
					if (V[I[k + i] + h] < x) {
						x = V[I[k + i] + h];
						j = 0;
					}

					if (V[I[k + i] + h] == x) {
						tmp = I[k + j];
						I[k + j] = I[k + i];
						I[k + i] = tmp;
						j++;
					}

				}

				for (i = 0; i < j; i++) {
					V[I[k + i]] = k + j - 1;
				}
				if (j == 1) {
					I[k] = -1;
				}
			}

			return;
		}

		x = V[I[start + len / 2] + h];
		jj = 0;
		kk = 0;
		for (i = start; i < start + len; i++) {
			if (V[I[i] + h] < x) {
				jj++;
			}
			if (V[I[i] + h] == x) {
				kk++;
			}
		}

		jj += start;
		kk += jj;

		i = start;
		j = 0;
		k = 0;
		while (i < jj) {
			if (V[I[i] + h] < x) {
				i++;
			} else if (V[I[i] + h] == x) {
				tmp = I[i];
				I[i] = I[jj + j];
				I[jj + j] = tmp;
				j++;
			} else {
				tmp = I[i];
				I[i] = I[kk + k];
				I[kk + k] = tmp;
				k++;
			}

		}

		while (jj + j < kk) {
			if (V[I[jj + j] + h] == x) {
				j++;
			} else {
				tmp = I[jj + j];
				I[jj + j] = I[kk + k];
				I[kk + k] = tmp;
				k++;
			}

		}

		if (jj > start) {
			split(I, V, start, jj - start, h);
		}

		for (i = 0; i < kk - jj; i++) {
			V[I[jj + i]] = kk - 1;
		}

		if (jj == kk - 1) {
			I[jj] = -1;
		}

		if (start + len > kk) {
			split(I, V, kk, start + len - kk, h);
		}

	}

	/**
	 * Fast suffix sporting. Larsson and Sadakane's qsufsort algorithm. See
	 * http://www.cs.lth.se/Research/Algorithms/Papers/jesper5.ps
	 * 
	 * @param I
	 * @param V
	 * @param oldBuf
	 * @param oldsize
	 */
	private static void qsufsort(int[] I, int[] V, byte[] oldBuf, int oldsize) {

		// int oldsize = oldBuf.length;
		int[] buckets = new int[256];

		// No need to do that in Java.
		// for ( int i = 0; i < 256; i++ ) {
		// buckets[i] = 0;
		// }

		for (int i = 0; i < oldsize; i++) {
			buckets[oldBuf[i] & 0xff]++;
		}

		for (int i = 1; i < 256; i++) {
			buckets[i] += buckets[i - 1];
		}

		for (int i = 255; i > 0; i--) {
			buckets[i] = buckets[i - 1];
		}

		buckets[0] = 0;

		for (int i = 0; i < oldsize; i++) {
			I[++buckets[oldBuf[i] & 0xff]] = i;
		}

		I[0] = oldsize;
		for (int i = 0; i < oldsize; i++) {
			V[i] = buckets[oldBuf[i] & 0xff];
		}
		V[oldsize] = 0;

		for (int i = 1; i < 256; i++) {
			if (buckets[i] == buckets[i - 1] + 1) {
				I[buckets[i]] = -1;
			}
		}

		I[0] = -1;

		for (int h = 1; I[0] != -(oldsize + 1); h += h) {
			int len = 0;
			int i;
			for (i = 0; i < oldsize + 1;) {
				if (I[i] < 0) {
					len -= I[i];
					i -= I[i];
				} else {
					// if(len) I[i-len]=-len;
					if (len != 0) {
						I[i - len] = -len;
					}
					len = V[I[i]] + 1 - i;
					split(I, V, i, len, h);
					i += len;
					len = 0;
				}

			}

			if (len != 0) {
				I[i - len] = -len;
			}
		}

		for (int i = 0; i < oldsize + 1; i++) {
			I[V[i]] = i;
		}
	}

	/**
	 * Count the number of bytes that match in oldBuf (starting at offset
	 * oldOffset) and newBuf (starting at offset newOffset).
	 * 
	 * @param oldBuf
	 * @param oldOffset
	 * @param newBuf
	 * @param newOffset
	 * @return
	 */
	private final static int matchlen(byte[] oldBuf, int oldSize,
			int oldOffset, byte[] newBuf, int newSize, int newOffset) {
		// int end = Math
		// .min(oldBuf.length - oldOffset, newBuf.length - newOffset);
		int end = Math.min(oldSize - oldOffset, newSize - newOffset);
		for (int i = 0; i < end; i++) {
			if (oldBuf[oldOffset + i] != newBuf[newOffset + i]) {
				return i;
			}
		}
		return end;
	}

	private final static int search(int[] I, byte[] oldBuf, int oldSize,
			byte[] newBuf, int newSize, int newBufOffset, int start, int end,
			IntByRef pos) {

		if (end - start < 2) {
			int x = matchlen(oldBuf, oldSize, I[start], newBuf, newSize,
					newBufOffset);
			int y = matchlen(oldBuf, oldSize, I[end], newBuf, newSize,
					newBufOffset);

			if (x > y) {
				pos.value = I[start];
				return x;
			} else {
				pos.value = I[end];
				return y;
			}
		}

		int x = start + (end - start) / 2;
		if (Util.memcmp(oldBuf, oldSize, I[x], newBuf, newSize, newBufOffset) < 0) {
			return search(I, oldBuf, oldSize, newBuf, newSize, newBufOffset, x,
					end, pos);
		} else {
			return search(I, oldBuf, oldSize, newBuf, newSize, newBufOffset,
					start, x, pos);
		}

	}

	/**
	 * @param oldFile
	 * @param newFile
	 * @param diffFile
	 * @throws IOException
	 */
	public static void bsdiff(File oldFile, File newFile, File diffFile)
			throws IOException {
		InputStream oldInputStream = new BufferedInputStream(
				new FileInputStream(oldFile));
		InputStream newInputStream = new BufferedInputStream(
				new FileInputStream(newFile));
		OutputStream diffOutputStream = new FileOutputStream(diffFile);

		byte[] diffBytes = bsdiff(oldInputStream, (int) oldFile.length(),
				newInputStream, (int) newFile.length());

		diffOutputStream.write(diffBytes);
		diffOutputStream.close();
	}

	/**
	 * @param oldInputStream
	 * @param oldsize
	 * @param newInputStream
	 * @param newsize
	 * @return
	 * @throws IOException
	 */
	public static byte[] bsdiff(InputStream oldInputStream, int oldsize,
			InputStream newInputStream, int newsize) throws IOException {

		byte[] oldBuf = new byte[oldsize];

		Util.readFromStream(oldInputStream, oldBuf, 0, oldsize);
		oldInputStream.close();

		byte[] newBuf = new byte[newsize];
		Util.readFromStream(newInputStream, newBuf, 0, newsize);
		newInputStream.close();

		return bsdiff(oldBuf, oldsize, newBuf, newsize);
	}

	/**
	 * @param oldBuf
	 * @param oldsize
	 * @param newBuf
	 * @param newsize
	 * @return
	 * @throws IOException
	 */
	public static byte[] bsdiff(byte[] oldBuf, int oldsize, byte[] newBuf,
			int newsize) throws IOException {

		int[] I = new int[oldsize + 1];
		qsufsort(I, new int[oldsize + 1], oldBuf, oldsize);

		// diff block
		int dblen = 0;
		byte[] db = new byte[newsize];

		// extra block
		int eblen = 0;
		byte[] eb = new byte[newsize];

		/*
		 * Diff file is composed as follows:
		 * 
		 * Header (32 bytes) Data (from offset 32 to end of file)
		 * 
		 * Header: Offset 0, length 8 bytes: file magic "jbdiff40" Offset 8,
		 * length 8 bytes: length of compressed ctrl block Offset 16, length 8
		 * bytes: length of compressed diff block Offset 24, length 8 bytes:
		 * length of new file
		 * 
		 * Data: 32 (length ctrlBlockLen): ctrlBlock (bzip2) 32+ctrlBlockLen
		 * (length diffBlockLen): diffBlock (bzip2) 32+ctrlBlockLen+diffBlockLen
		 * (to end of file): extraBlock (bzip2)
		 * 
		 * ctrlBlock comprises a set of records, each record 12 bytes. A record
		 * comprises 3 x 32 bit integers. The ctrlBlock is not compressed.
		 */

		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		DataOutputStream diffOut = new DataOutputStream(byteOut);

		/*
		 * Write as much of header as we have now. Size of ctrlBlock and
		 * diffBlock must be filled in later.
		 */
		diffOut.write(MAGIC_BYTES);
		diffOut.writeLong(-1); // place holder for ctrlBlockLen
		diffOut.writeLong(-1); // place holder for diffBlockLen
		diffOut.writeLong(newsize);
		diffOut.flush();

		GZIPOutputStream bzip2Out = new GZIPOutputStream(diffOut);
		DataOutputStream dataOut = new DataOutputStream(bzip2Out);

		int oldscore, scsc;

		int overlap, Ss, lens;
		int i;
		int scan = 0;
		int len = 0;
		int lastscan = 0;
		int lastpos = 0;
		int lastoffset = 0;

		IntByRef pos = new IntByRef();
		// int ctrlBlockLen = 0;

		while (scan < newsize) {
			oldscore = 0;

			for (scsc = scan += len; scan < newsize; scan++) {

				len = search(I, oldBuf, oldsize, newBuf, newsize, scan, 0,
						oldsize, pos);

				for (; scsc < scan + len; scsc++) {
					if ((scsc + lastoffset < oldsize)
							&& (oldBuf[scsc + lastoffset] == newBuf[scsc])) {
						oldscore++;
					}
				}

				if (((len == oldscore) && (len != 0)) || (len > oldscore + 8)) {
					break;
				}

				if ((scan + lastoffset < oldsize)
						&& (oldBuf[scan + lastoffset] == newBuf[scan])) {
					oldscore--;
				}
			}

			if ((len != oldscore) || (scan == newsize)) {
				int s = 0;
				int Sf = 0;
				int lenf = 0;
				for (i = 0; (lastscan + i < scan) && (lastpos + i < oldsize);) {
					if (oldBuf[lastpos + i] == newBuf[lastscan + i])
						s++;
					i++;
					if (s * 2 - i > Sf * 2 - lenf) {
						Sf = s;
						lenf = i;
					}
				}

				int lenb = 0;
				if (scan < newsize) {
					s = 0;
					int Sb = 0;
					for (i = 1; (scan >= lastscan + i) && (pos.value >= i); i++) {
						if (oldBuf[pos.value - i] == newBuf[scan - i])
							s++;
						if (s * 2 - i > Sb * 2 - lenb) {
							Sb = s;
							lenb = i;
						}
					}
				}

				if (lastscan + lenf > scan - lenb) {
					overlap = (lastscan + lenf) - (scan - lenb);
					s = 0;
					Ss = 0;
					lens = 0;
					for (i = 0; i < overlap; i++) {
						if (newBuf[lastscan + lenf - overlap + i] == oldBuf[lastpos
								+ lenf - overlap + i]) {
							s++;
						}
						if (newBuf[scan - lenb + i] == oldBuf[pos.value - lenb
								+ i]) {
							s--;
						}
						if (s > Ss) {
							Ss = s;
							lens = i + 1;
						}
					}

					lenf += lens - overlap;
					lenb -= lens;
				}

				// ? byte casting introduced here -- might affect things
				for (i = 0; i < lenf; i++) {
					db[dblen + i] = (byte) (newBuf[lastscan + i] - oldBuf[lastpos
							+ i]);
				}

				for (i = 0; i < (scan - lenb) - (lastscan + lenf); i++) {
					eb[eblen + i] = newBuf[lastscan + lenf + i];
				}

				dblen += lenf;
				eblen += (scan - lenb) - (lastscan + lenf);

				/*
				 * Write control block entry (3 x int)
				 */
				// diffOut.writeInt( lenf );
				// diffOut.writeInt( ( scan - lenb ) - ( lastscan + lenf ) );
				// diffOut.writeInt( ( pos[0] - lenb ) - ( lastpos + lenf ) );
				// ctrlBlockLen += 12;
				dataOut.writeInt(lenf);
				dataOut.writeInt((scan - lenb) - (lastscan + lenf));
				dataOut.writeInt((pos.value - lenb) - (lastpos + lenf));

				lastscan = scan - lenb;
				lastpos = pos.value - lenb;
				lastoffset = pos.value - scan;
			} // end if
		} // end while loop

		dataOut.flush();
		bzip2Out.finish();

		// now compressed ctrlBlockLen
		int ctrlBlockLen = diffOut.size() - Util.HEADER_SIZE;
		// System.err.println( "Diff: ctrlBlockLen=" + ctrlBlockLen );

		// GZIPOutputStream gzOut;

		/*
		 * Write diff block
		 */
		// gzOut = new GZIPOutputStream( diffOut );
		bzip2Out = new GZIPOutputStream(diffOut);
		bzip2Out.write(db, 0, dblen);
		bzip2Out.finish();
		bzip2Out.flush();
		int diffBlockLen = diffOut.size() - ctrlBlockLen - Util.HEADER_SIZE;
		// System.err.println( "Diff: diffBlockLen=" + diffBlockLen );

		/*
		 * Write extra block
		 */
		// gzOut = new GZIPOutputStream( diffOut );
		bzip2Out = new GZIPOutputStream(diffOut);
		bzip2Out.write(eb, 0, eblen);
		bzip2Out.finish();
		bzip2Out.flush();
		// long extraBlockLen = diffOut.size() - diffBlockLen - ctrlBlockLen -
		// HEADER_SIZE;
		// System.err.println( "Diff: extraBlockLen=" + extraBlockLen );

		diffOut.close();

		/*
		 * Write missing header info.
		 */
		ByteArrayOutputStream byteHeaderOut = new ByteArrayOutputStream(
				Util.HEADER_SIZE);
		DataOutputStream headerOut = new DataOutputStream(byteHeaderOut);
		headerOut.write(MAGIC_BYTES);
		headerOut.writeLong(ctrlBlockLen); // place holder for ctrlBlockLen
		headerOut.writeLong(diffBlockLen); // place holder for diffBlockLen
		headerOut.writeLong(newsize);
		headerOut.close();

		// Copy header information into the diff
		byte[] diffBytes = byteOut.toByteArray();
		byte[] headerBytes = byteHeaderOut.toByteArray();

		System.arraycopy(headerBytes, 0, diffBytes, 0, headerBytes.length);

		return diffBytes;
		// /*
		// * Write missing header info. Need to reopen the file with
		// RandomAccessFile
		// * for this.
		// */
		// RandomAccessFile diff = new RandomAccessFile( diffFile, "rw" );
		// diff.seek( 8 );
		// diff.writeLong( ctrlBlockLen ); // ctrlBlockLen (compressed) @offset
		// 8
		// diff.writeLong( diffBlockLen ); // diffBlockLen (compressed) @offset
		// 16
		// diff.close();
	}

	/**
	 * Run JBDiff from the command line. Params: oldfile newfile difffile. diff
	 * file will be created.
	 * 
	 * @param arg
	 * @throws IOException
	 */
	public static void main(String[] arg) throws IOException {

		if (arg.length != 3) {
			System.err
					.println("usage example: java -Xmx200m ie.wombat.jbdiff.JBDiff oldfile newfile patchfile\n");
			return;
		}

		File oldFile = new File(arg[0]);
		File newFile = new File(arg[1]);
		File diffFile = new File(arg[2]);

		bsdiff(oldFile, newFile, diffFile);

	}

	private static class IntByRef {
		private int value;
	}
}
