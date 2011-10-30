package com.tippingpoint.conscan.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.tippingpoint.conscan.objects.BusinessObject;
import com.tippingpoint.conscan.objects.BusinessObjectBuilder;
import com.tippingpoint.conscan.objects.BusinessObjectBuilderFactory;
import com.tippingpoint.sql.SqlBaseException;

public class ScannerLogCommentService extends BaseTableService {
	private static final long serialVersionUID = 2318703620281811532L;

	public ScannerLogCommentService() {
		super("scannerlogcomment");
	}

	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		final PrintWriter out = returnXml(response, HttpServletResponse.SC_OK);

		final Map<String, String> mapParameters = getParameterMap(request);
		final String strScannerLogGuid = mapParameters.get("scannerlogguid");
		final String strNotes = mapParameters.get("notes");

		try {
			final BusinessObject boLogComment = getScannerLogComment(strScannerLogGuid, strNotes);
			if (boLogComment != null) {
				writeObject(out, boLogComment, false);
			}
		}
		catch (final SqlBaseException e) {
			// eat for now
		}
	}

	/**
	 * This method returns the user for the given parameters.
	 * 
	 * @throws SqlBaseException
	 */
	private BusinessObject getScannerLogComment(final String strScannerLogId, final String strNoteText)
			throws SqlBaseException {
		BusinessObject boScannerLogComment = null;
		if (strScannerLogId != null) {
			if (strNoteText != null) {
				final BusinessObjectBuilder builder =
					BusinessObjectBuilderFactory.get().getBuilder("scannerlogcomment");
				boScannerLogComment = builder.get();
				boScannerLogComment.setValue("scannerlogid", strScannerLogId);
				boScannerLogComment.setValue("comment", strNoteText);
				boScannerLogComment.save();
			}
		}

		return boScannerLogComment;
	}

}
