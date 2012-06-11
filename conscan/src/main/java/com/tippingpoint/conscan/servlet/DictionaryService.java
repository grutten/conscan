package com.tippingpoint.conscan.servlet;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.simple.JSONObject;
import com.sun.jersey.spi.resource.Singleton;
import com.tippingpoint.conscan.objects.BusinessObject;
import com.tippingpoint.conscan.objects.BusinessObjectBuilder;
import com.tippingpoint.conscan.objects.BusinessObjectBuilderFactory;
import com.tippingpoint.conscan.objects.FieldValue;
import com.tippingpoint.conscan.objects.json.JsonBusinessObject;
import com.tippingpoint.sql.SqlBaseException;

/**
 * This class is used to service the dictionary requests.
 */
@Singleton
@Path("/dictionary")
public class DictionaryService extends ServiceBase {
	public DictionaryService() {
		super("dictionary");
	}

	/**
	 * This method returns the staff object by id.
	 * 
	 * @throws SqlBaseException
	 */
	@GET
	@Path("/{term:[a-zA-Z\\.]+}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getJsonObjectByTerm(@PathParam("term") final String strTerm) throws SqlBaseException {
		// attempt to get the object by term
		String strJson = null;
		final BusinessObject businessObject = getObjectByTerm(strTerm);
		if (businessObject != null) {
			final JsonBusinessObject jsonBusinessObject = new JsonBusinessObject(businessObject);

			final JSONObject jsonObject = jsonBusinessObject.get();
			if (jsonObject == null) {
				throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
			}

			strJson = jsonObject.toJSONString();
		}

		return strJson;
	}

	/**
	 * This method is used to look up the business object based on id.
	 * 
	 * @param strId String containing the id of the object.
	 * @throws SqlBaseException
	 */
	private BusinessObject getObjectByTerm(final String strTerm) throws SqlBaseException {
		// determine if the builder for the local object type is available
		final BusinessObjectBuilder builder = BusinessObjectBuilderFactory.get().getBuilder(m_strBusinessObjectType);
		if (builder == null) {
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}

		// look up the dictionary term
		final List<FieldValue> listParameters = new ArrayList<FieldValue>();

		listParameters.add(new FieldValue("term", strTerm));

		BusinessObject boTerm = null;
		final List<BusinessObject> listBusinessObject = builder.getAll(listParameters);
		if (listBusinessObject != null && listBusinessObject.size() == 1) {
			boTerm = listBusinessObject.get(0);
		}

		return boTerm;
	}
}
