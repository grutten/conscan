package com.tippingpoint.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * The motivation for this class is that the XML generated by the scanner does
 * not contain a closing tag because the scanner never knows when it is finished
 * recording its output.
 * @author mgee
 *
 */
public class ScannerInputStreamReader extends InputStreamReader {
	private String m_strTagName;  // Does not include tag braces
//	private int m_nLengthOfTag;
//	private int m_nCharactersReturned;
	
	public ScannerInputStreamReader(InputStream in, String strTagName) {
		super(in);
		
		m_strTagName = strTagName;
//		m_nLengthOfTag = m_strTagName.length();
//		m_nCharactersReturned = 0;
	}

	@Override
	public int read() throws IOException {
		System.out.println("read()");
		
		return super.read();
	}

	@Override
	public int read(char[] cbuf) throws IOException {
		System.out.println("read(char[] cbuf)");
		
		return super.read(cbuf);
	}
	
	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		int nCharsRead = super.read(cbuf, off, len);
//		System.out.println("@override read - off, len, charsRead: {" + Integer.valueOf(off).toString() + ", " + 
//				Integer.valueOf(len) + ", " + Integer.valueOf(nCharsRead) + "}");
		
		if (nCharsRead >= 0 && nCharsRead < len) {
			String strEndLiteral = "</" + m_strTagName + ">";
			int nEndLiteralLength = strEndLiteral.length();
			char[] cbufEndLiteral = strEndLiteral.toCharArray();
			for (int i = 0; i < nEndLiteralLength; ++i)
				cbuf[off + nCharsRead + i] = cbufEndLiteral[i];
			nCharsRead += nEndLiteralLength;		
		}
		
		return nCharsRead;
	}
	
}
