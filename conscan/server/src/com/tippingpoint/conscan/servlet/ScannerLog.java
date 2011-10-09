package com.tippingpoint.conscan.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tippingpoint.conscan.objects.BusinessObject;
import com.tippingpoint.xml.Data;

public class ScannerLog extends BaseTableService {
	private static final long serialVersionUID = 8403913752494861335L;

	private static Log m_log = LogFactory.getLog(ScannerLogService.class);
	
	public ScannerLog() {
		super("scannerlog");
	}
	
	/**
	 * This method performs the post action.
	 * 
	 * @throws IOException
	 */
	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		// parse the request
		try {
			// check that we have a file upload request
			final boolean isMultipart = ServletFileUpload.isMultipartContent(request);
			if (isMultipart) {
				// create a new file upload handler
				final ServletFileUpload upload = new ServletFileUpload();

				final PrintWriter out = returnXml(response, HttpServletResponse.SC_OK);

				out.println("<objects>");

				final FileItemIterator iter = upload.getItemIterator(request);
				while (iter.hasNext()) {
					final FileItemStream fileItemStream = iter.next();
					final String strName = fileItemStream.getFieldName();
					final InputStream stream = fileItemStream.openStream();
					if (fileItemStream.isFormField()) {
						System.out.println("Form field " + strName + " with value " + Streams.asString(stream) +
								" detected.");
					}
					else {
						System.out.println("File field " + strName + " with file name " + fileItemStream.getName() +
								" detected.");

						final BufferedReader readerData = new BufferedReader(new InputStreamReader(stream));
						final Data dataTippingPointServer = new Data(readerData);

						final ArrayList<BusinessObject> arr = dataTippingPointServer.get();

						final Iterator<BusinessObject> iterBusinessObject = arr.iterator();
						while (iterBusinessObject.hasNext()) {
							final BusinessObject currBusinessObject = iterBusinessObject.next();
							writeObject(out, currBusinessObject, false);
						}
					}
				}

				out.println("</objects>");

			}
			else {
				throw new IllegalArgumentException("Log posts must be multi-part.");
			}
		}
		catch (final FileUploadException e) {
			m_log.error("File upload error.", e);
			processException(response, e);
		}
		catch (final IOException e) {
			m_log.error("I/O error reading file.", e);
			processException(response, e);
		}
		catch (final Exception e) {
			m_log.error("Error reading file.", e);
			processException(response, e);
		}
	}
	
}
