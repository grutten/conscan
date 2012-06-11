package com.tippingpoint.conscan.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import org.json.simple.JSONObject;
import com.tippingpoint.conscan.objects.BusinessObject;
import com.tippingpoint.conscan.objects.BusinessObjectBuilder;
import com.tippingpoint.conscan.objects.BusinessObjectBuilderFactory;
import com.tippingpoint.conscan.objects.FieldValue;
import com.tippingpoint.conscan.objects.JsonBusinessObjectList;
import com.tippingpoint.conscan.objects.json.JsonBusinessObject;
import com.tippingpoint.sql.SqlBaseException;
import com.tippingpoint.utilities.NameValuePair;
import com.tippingpoint.utilities.XmlUtilities;

/**
 * This class is the base class for all rest services.
 */
public abstract class ServiceBase {
	@Context
	protected ServletContext context;

	/** This member holds the name of the business object type. */
	protected String m_strBusinessObjectType;

	/**
	 * THis method constructs a new service for the given business object type.
	 * 
	 * @param strBusinessObjectType String containing the business object type.
	 */
	public ServiceBase(final String strBusinessObjectType) {
		m_strBusinessObjectType = strBusinessObjectType;
	}

	/**
	 * This method adds a new object.
	 * 
	 * @throws SqlBaseException
	 */
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public void addObject(final MultivaluedMap<String, String> mapValues) throws SqlBaseException {
		// determine if the builder for the local object type is available
		final BusinessObjectBuilder builder = BusinessObjectBuilderFactory.get().getBuilder(m_strBusinessObjectType);
		if (builder == null) {
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}

		final BusinessObject businessObject = builder.get();
		if (businessObject == null) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}

		populateObject(businessObject, mapValues);
	}

	/**
	 * This method deletes an existing object.
	 * 
	 * @throws SqlBaseException
	 */
	@DELETE
	@Path("/{id:[a-f0-9\\-]+}")
	public void deleteObject(@PathParam("id") final String strId) throws SqlBaseException {
		// get the object by the id
		final BusinessObject businessObject = getObjectById(strId);
		if (businessObject != null) {
			businessObject.delete();
		}
	}

	/**
	 * This method returns the staff object by id.
	 * 
	 * @throws SqlBaseException
	 */
	@GET
	@Path("/{id:[a-f0-9\\-]+}")
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
	 * This method returns a collection of objects.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public StreamingOutput getJsonObjects() {

		// TODO: the code from BaseTableService.writeObjects() also set
		// the respose status and contenttype. Does JAX-rs do that for me?
		return new ServiceOutputJson(m_strBusinessObjectType.toString());
	}

	/**
	 * This method redirects to the referenced table based on the idref type column.
	 */
	@GET
	@Path("/{columnName:[a-z]+}")
	@Produces(MediaType.APPLICATION_JSON)
	public StreamingOutput getJsonReferenceObject(@PathParam("columnName") final String strColumnName) {
		// determine if the builder for the local object type is available
		BusinessObjectBuilder builder = BusinessObjectBuilderFactory.get().getBuilder(m_strBusinessObjectType);
		if (builder == null) {
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}

		StreamingOutput output = null;

		final String strReferencedObjectType = builder.getReferencedObjectType(strColumnName);
		if (strReferencedObjectType != null) {
			builder = BusinessObjectBuilderFactory.get().getBuilder(strReferencedObjectType);
			if (builder == null) {
				throw new WebApplicationException(Response.Status.BAD_REQUEST);
			}

			output = new ServiceOutputJson(strReferencedObjectType);
		}

		return output;
	}

	/**
	 * This method returns a collection of objects.
	 */
	/*
	 * @GET
	 * @Produces(MediaType.APPLICATION_XML) public StreamingOutput getXmlObjects() { return new
	 * ServiceOutputXml(m_strBusinessObjectType.toString()); }
	 */

	/**
	 * This method updates the staff object.
	 * 
	 * @throws SqlBaseException
	 */
	@PUT
	@Path("/{id:[a-f0-9\\-]+}")
	public void updateObjectById(@PathParam("id") final String strId, final MultivaluedMap<String, String> mapValues)
			throws SqlBaseException {
		// get the object by the id
		final BusinessObject businessObject = getObjectById(strId);
		if (businessObject != null) {
			populateObject(businessObject, mapValues);
		}
	}

	/**
	 * This method is used to look up the business object based on id.
	 * 
	 * @param strId String containing the id of the object.
	 * @throws SqlBaseException
	 */
	private BusinessObject getObjectById(final String strId) throws SqlBaseException {
		// determine if the builder for the local object type is available
		final BusinessObjectBuilder builder = BusinessObjectBuilderFactory.get().getBuilder(m_strBusinessObjectType);
		if (builder == null) {
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}

		final BusinessObject businessObject = builder.get(strId);
		if (businessObject == null) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}

		return businessObject;
	}

	/**
	 * THis method populates the business object and saves it.
	 * 
	 * @throws SqlBaseException
	 */
	private void populateObject(final BusinessObject businessObject, final MultivaluedMap<String, String> mapValues)
			throws SqlBaseException {
		// update the business object
		final Iterator<String> iterFields = businessObject.getFields();
		if (iterFields != null && iterFields.hasNext()) {
			final FieldValue identifierField = businessObject.getIdentifierField();

			// loop through the business object fields for the names of the properties
			while (iterFields.hasNext()) {
				final String strField = iterFields.next();
				if (identifierField == null || !strField.equals(identifierField.getName())) {
					final List<String> listValues = mapValues.get(strField);
					if (listValues != null && !listValues.isEmpty()) {
						businessObject.setValue(strField, listValues.get(0));
					}
				}
			}
		}

		// save the business object
		businessObject.save();
	}

	/**
	 * This class is used generate the output of the business object. The specific format is deferred to child classes.
	 */
	protected abstract class ServiceOutput implements StreamingOutput {
		protected String m_strObjectName;

		/**
		 * This method constructs the stream object used for rendering web pages.
		 * 
		 * @param strObjetName
		 */
		public ServiceOutput(final String strObjectName) {
			m_strObjectName = strObjectName;
		}

		@Override
		public void write(final OutputStream out) {
			final BusinessObjectBuilder builder = BusinessObjectBuilderFactory.get().getBuilder(m_strObjectName);
			if (builder != null) {
				try {
					final List<BusinessObject> listObjects = builder.getAll();
					if (listObjects != null && !listObjects.isEmpty()) {
						writeObjects(out, listObjects);
					}
				}
				catch (final SqlBaseException e) {
					throw new IllegalStateException(e);
				}
			}
		}

		protected abstract void writeObjects(final OutputStream out, final List<BusinessObject> listObjects);
	}

	/**
	 * This class is used to generate the output of the business object as JSON.
	 */
	protected class ServiceOutputJson extends ServiceOutput implements StreamingOutput {
		public ServiceOutputJson(final String strObjectName) {
			super(strObjectName);
		}

		@Override
		protected void writeObjects(final OutputStream out, final List<BusinessObject> listObjects) {
			final JsonBusinessObjectList jsonObjects = new JsonBusinessObjectList(listObjects);
			try {
				out.write(jsonObjects.get().toString().getBytes());
			}
			catch (final IOException e) {
				throw new IllegalStateException(e);
			}
		}
	}

	/**
	 * This class is used to generate the output of the business object as XML.
	 */
	protected class ServiceOutputXml extends ServiceOutput implements StreamingOutput {
		public ServiceOutputXml(final String strObjectName) {
			super(strObjectName);
		}

		@Override
		protected void writeObjects(final OutputStream out, final List<BusinessObject> listObjects) {
			final JsonBusinessObjectList jsonObjects = new JsonBusinessObjectList(listObjects);
			try {
				out.write(XmlUtilities.open(XmlTags.TAG_LIST,
						new NameValuePair(XmlTags.ATTRIBUTE_NAME, listObjects.get(0).getType())).getBytes());

				/*
				 * SEE Services.writeObject() for (final BusinessObject businessObject : listObjects) { writeObject(out,
				 * businessObject, false); }
				 */
				out.write(XmlUtilities.close(XmlTags.TAG_LIST).getBytes());
			}
			catch (final IOException e) {
				throw new IllegalStateException(e);
			}
		}
	}
}
