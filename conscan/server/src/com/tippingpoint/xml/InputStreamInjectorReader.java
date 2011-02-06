package com.tippingpoint.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class InputStreamInjectorReader extends InputStreamReader {
	private String m_strTagName;  // Does not include tag braces
	private int m_nLengthOfTag;
	private int m_nCharactersReturned;
	
	public InputStreamInjectorReader(InputStream in, String strTagName) {
		super(in);
		
		m_strTagName = strTagName;
		m_nLengthOfTag = m_strTagName.length();
		m_nCharactersReturned = 0;
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
		String strBeginLiteral = "<" + m_strTagName + ">";
		int nBeginLiteralLength = 0;
		int nCharsRead = 0;
		
		if (m_nCharactersReturned == 0) {
			nBeginLiteralLength = strBeginLiteral.length();
			char[] cbufLiteral = strBeginLiteral.toCharArray();
			for (int i = 0; i < nBeginLiteralLength; ++i)
				cbuf[off + i] = cbufLiteral[i];
			nCharsRead = nBeginLiteralLength;
		}
		
		int nCalculatedLen = len - nBeginLiteralLength;
		int nOffsetForFirstCopyOperation = off +  nBeginLiteralLength;
		nCharsRead += super.read(cbuf, nOffsetForFirstCopyOperation, nCalculatedLen);
		
		if (nCharsRead < nCalculatedLen) {
			String strEndLiteral = "</" + m_strTagName + ">";
			int nEndLiteralLength = strEndLiteral.length();
			char[] cbufEndLiteral = strEndLiteral.toCharArray();
			for (int i = 0; i < nEndLiteralLength; ++i)
				cbuf[nOffsetForFirstCopyOperation + nCharsRead + i] = cbufEndLiteral[i];
			nCharsRead += nEndLiteralLength;
		}
		
		System.out.println("buf: " + new String(cbuf));
		
		return nCharsRead;
	}
}
