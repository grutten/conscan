package com.tippingpoint.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ScannerInputStreamReader extends InputStreamReader {
	private String m_strTagName;  // Does not include tag braces
	private int m_nLengthOfTag;
	private int m_nCharactersReturned;
	
	public ScannerInputStreamReader(InputStream in, String strTagName) {
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
		System.out.println("@override read");
		return super.read(cbuf, off, len);
		
	}
}
