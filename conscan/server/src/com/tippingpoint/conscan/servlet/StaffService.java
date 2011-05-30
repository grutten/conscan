package com.tippingpoint.conscan.servlet;

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
import com.tippingpoint.conscan.objects.json.JsonBusinessObject;
import com.tippingpoint.sql.SqlBaseException;

/**
 * This class is used to service the staff requests.
 */
@Singleton
@Path("/staff")
public class StaffService {
	private static final String TABLE_NAME = "staff";

	/**
	 * This method returns the staff object by id.
	 * 
	 * @throws SqlBaseException
	 */
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getJsonObjectById(@PathParam("id") final String strId) throws SqlBaseException {
		String strJson = null;
		final BusinessObject businessObject = getObjectById(strId);
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
	private BusinessObject getObjectById(final String strId) throws SqlBaseException {
		// determine if the builder for the local object type is available
		final BusinessObjectBuilder builder = BusinessObjectBuilderFactory.get().getBuilder(TABLE_NAME);
		if (builder == null) {
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}

		final BusinessObject businessObject = builder.get(strId);
		if (businessObject == null) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}

		return businessObject;
	}
}
