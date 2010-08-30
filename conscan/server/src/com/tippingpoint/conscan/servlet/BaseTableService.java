package com.tippingpoint.conscan.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.tippingpoint.conscan.objects.BusinessObject;
import com.tippingpoint.conscan.objects.BusinessObjectBuilder;
import com.tippingpoint.conscan.objects.BusinessObjectBuilderFactory;
import com.tippingpoint.sql.SqlBaseException;
import com.tippingpoint.utilities.NameValuePair;
import com.tippingpoint.utilities.XmlUtilities;

/**
 * This class serves the <base table service> for related objects.
 */
public class BaseTableService extends Services {
	private static Log m_log = LogFactory.getLog(BaseTableService.class);
	private static final long serialVersionUID = 6661798263642526305L;
	
	private String m_strTableName;

	/**
	 * This constructor sets the name of the underlying table.
	 * @param strTableName
	 */
	public BaseTableService(String strTableName) {
		m_strTableName = strTableName;
	}
	
	/**
	 * This method executes the get command; which is used to return the contents of a <base table service>.
	 * 
	 * @throws IOException
	 */
	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		final String strPathInfo = request.getPathInfo();

		m_log.debug("Get: " + strPathInfo);

		final List<String> listElements = getObjects(strPathInfo);
		if (listElements != null && !listElements.isEmpty()) {
			final BusinessObjectBuilder builder = BusinessObjectBuilderFactory.get().getBuilder(m_strTableName);
			if (builder != null) {
				try {
					switch (listElements.size()) {
					case 1:
						final BusinessObject businessObject = builder.get(listElements.get(0));
						if (businessObject != null) {
							returnObject(response, businessObject);
						}
					break;

					default:
						// throw an error?
					break;
					}
				}
				catch (final SqlBaseException e) {
					m_log.error("Database error retrieving " + m_strTableName + " object.", e);
					processException(response, e);
				}
			}
			else {
				returnXml(response, HttpServletResponse.SC_NO_CONTENT);
			}
		}
		else {
			returnXml(response, HttpServletResponse.SC_NO_CONTENT);
		}
	}

	/**
	 * This method breaks down the string used to identify the object.
	 * 
	 * @param strPathInfo String containing the path information.
	 */
	private List<String> getObjects(final String strPathInfo) {
		final List<String> listElements = new ArrayList<String>();

		// convert the path information to an array of strings
		if (StringUtils.isNotBlank(strPathInfo)) {
			final StringTokenizer tokenizer = new StringTokenizer(strPathInfo, "/");
			while (tokenizer.hasMoreTokens()) {
				final String strElement = StringUtils.trimToNull(tokenizer.nextToken());
				if (strElement != null) {
					listElements.add(strElement);
				}
			}
		}

		return listElements;
	}

	/**
	 * This method returns the passed in object.
	 * 
	 * @param response HttpServletResponse where the results are to be returned.
	 * @param businessObject BusinessObject to be returned.
	 * @throws IOException
	 */
	private void returnObject(final HttpServletResponse response, final BusinessObject businessObject)
			throws IOException {
		final PrintWriter writer = returnXml(response, HttpServletResponse.SC_OK);

		writer.append(XmlUtilities.open("object", new NameValuePair("type", businessObject.getType())));

		final Iterator<String> iterFields = businessObject.getFields();
		if (iterFields != null && iterFields.hasNext()) {
			while (iterFields.hasNext()) {
				final String strName = iterFields.next();
				writer.append(XmlUtilities.tag("field", Collections.singletonList(new NameValuePair("name", strName)),
						businessObject.getValue(strName)));
			}
		}

		writer.append(XmlUtilities.close("object"));
	}
}
