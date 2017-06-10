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
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;

/**
 * Java Binary patcher (based on bspatch by Colin Percival)
 * 
 * @author Joe Desbonnet, joe@galway.net
 */
public class JBPatch {

	// JBPatch extensions by Stefan.Liebig@compeople.de:
	//
	// - uses GZIP compressor to compress ALL of the blocks (ctrl,diff,extra).
	// - added an interface that allows using of JBPatch with streams and byte
	// arrays

//	private static final String VERSION = "jbdiff-0.1.0";

	/**
	 * Run JBPatch from the command line. Params: oldfile newfile patchfile.
	 * newfile will be created.
	 * 
	 * @param arg
	 * @throws IOException
	 */
	public static void main(String[] arg) throws IOException {

		if (arg.length != 3) {
			System.err
					.println("usage example: java -Xmx200m ie.wombat.jbdiff.JBPatch oldfile newfile patchfile");
		}

		File oldFile = new File(arg[0]);
		File newFile = new File(arg[1]);
		File diffFile = new File(arg[2]);

		bspatch(oldFile, newFile, diffFile);
	}

	/**
	 * @param oldFile
	 * @param newFile
	 * @param diffFile
	 * @throws IOException
	 */
	public static void bspatch(File oldFile, File newFile, File diffFile)
			throws IOException {
		InputStream oldInputStream = new BufferedInputStream(
				new FileInputStream(oldFile));
		byte[] diffBytes = new byte[(int) diffFile.length()];
		InputStream diffInputStream = new FileInputStream(diffFile);
		Util.readFromStream(diffInputStream, diffBytes, 0, diffBytes.length);

		byte[] newBytes = bspatch(oldInputStream, (int) oldFile.length(),
				diffBytes);

		OutputStream newOutputStream = new FileOutputStream(newFile);
		newOutputStream.write(newBytes);
		newOutputStream.close();
	}

	/**
	 * @param oldInputStream
	 * @param oldsize
	 * @param diffBytes
	 * @return
	 */
	public static byte[] bspatch(InputStream oldInputStream, int oldsize,
			byte[] diffBytes) throws IOException {
		/*
		 * Read in old file (file to be patched) to oldBuf
		 */
		// int oldsize = (int) oldFile.length();
		// byte[] oldBuf = new byte[oldsize + 1];
		byte[] oldBuf = new byte[oldsize];
		// InputStream oldIn = new FileInputStream( oldFile );
		Util.readFromStream(oldInputStream, oldBuf, 0, oldsize);
		oldInputStream.close();
		// oldIn.close();

		return JBPatch.bspatch(oldBuf, oldsize, diffBytes);
	}

	/**
	 * @param oldBuf
	 * @param oldsize
	 * @param diffBytes
	 * @return
	 * @throws IOException
	 */
	public static byte[] bspatch(byte[] oldBuf, int oldsize, byte[] diffBytes)
			throws IOException {
		return bspatch(oldBuf, oldsize, diffBytes, diffBytes.length);
	}

	/**
	 * @param oldBuf
	 * @param oldsize
	 * @param diffBuf
	 * @param diffSize
	 * @return
	 * @throws IOException
	 */
	public static byte[] bspatch(byte[] oldBuf, int oldsize, byte[] diffBuf,
			int diffSize) throws IOException {

		DataInputStream diffIn = new DataInputStream(new ByteArrayInputStream(
				diffBuf, 0, diffSize));

		// skip headerMagic at header offset 0 (length 8 bytes)
		diffIn.skip(8);

		// ctrlBlockLen after bzip2 compression at heater offset 8 (length 8
		// bytes)
		long ctrlBlockLen = diffIn.readLong();

		// diffBlockLen after bzip2 compression at header offset 16 (length 8
		// bytes)
		long diffBlockLen = diffIn.readLong();

		// size of new file at header offset 24 (length 8 bytes)
		int newsize = (int) diffIn.readLong();

		// System.err.println( "newsize=" + newsize );
		// System.err.println( "ctrlBlockLen=" + ctrlBlockLen );
		// System.err.println( "diffBlockLen=" + diffBlockLen );
		// System.err.println( "newsize=" + newsize );

		InputStream in;
		in = new ByteArrayInputStream(diffBuf, 0, diffSize);
		in.skip(Util.HEADER_SIZE);
		DataInputStream ctrlBlockIn = new DataInputStream(new GZIPInputStream(
				in));

		in = new ByteArrayInputStream(diffBuf, 0, diffSize);
		in.skip(ctrlBlockLen + Util.HEADER_SIZE);
		InputStream diffBlockIn = new GZIPInputStream(in);

		in = new ByteArrayInputStream(diffBuf, 0, diffSize);
		in.skip(diffBlockLen + ctrlBlockLen + Util.HEADER_SIZE);
		InputStream extraBlockIn = new GZIPInputStream(in);

		// byte[] newBuf = new byte[newsize + 1];
		byte[] newBuf = new byte[newsize];

		int oldpos = 0;
		int newpos = 0;
		int[] ctrl = new int[3];
		// int nbytes;
		while (newpos < newsize) {

			for (int i = 0; i <= 2; i++) {
				// ctrl[i] = diffIn.readInt();
				ctrl[i] = ctrlBlockIn.readInt();
				// System.err.println (" ctrl[" + i + "]=" + ctrl[i]);
			}

			if (newpos + ctrl[0] > newsize) {
				throw new IOException("Corrupt patch.");
			}

			/*
			 * Read ctrl[0] bytes from diffBlock stream
			 */

			Util.readFromStream(diffBlockIn, newBuf, newpos, ctrl[0]);

			for (int i = 0; i < ctrl[0]; i++) {
				if ((oldpos + i >= 0) && (oldpos + i < oldsize)) {
					newBuf[newpos + i] += oldBuf[oldpos + i];
				}
			}

			newpos += ctrl[0];
			oldpos += ctrl[0];

			if (newpos + ctrl[1] > newsize) {
				throw new IOException("Corrupt patch.");
			}

			Util.readFromStream(extraBlockIn, newBuf, newpos, ctrl[1]);

			newpos += ctrl[1];
			oldpos += ctrl[2];
		}

		//  Check if at end of ctrlIn
		//  Check if at the end of diffIn
		//  Check if at the end of extraIn

		// This check is not needed since the byte array has been allocated with
		// this constraint!
		// if ( newBuf.length - 1 != newsize ) {
		// throw new IOException( "Corrupt patch." );
		// }

		ctrlBlockIn.close();
		diffBlockIn.close();
		extraBlockIn.close();
		diffIn.close();

		return newBuf;
		// OutputStream out = new FileOutputStream( newFile );
		// out.write( newBuf, 0, newBuf.length - 1 );
		// out.close();
	}
}
