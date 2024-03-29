package com.tippingpoint.conscan.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

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

import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONObject;
import com.tippingpoint.conscan.objects.BusinessObject;
import com.tippingpoint.conscan.objects.BusinessObjectBuilder;
import com.tippingpoint.conscan.objects.BusinessObjectBuilderFactory;
import com.tippingpoint.conscan.objects.FieldValue;
import com.tippingpoint.conscan.objects.JsonBusinessObjectList;
import com.tippingpoint.conscan.objects.json.JsonBusinessObject;
import com.tippingpoint.database.ColumnDefinition;
import com.tippingpoint.database.Constraint;
import com.tippingpoint.database.DatabaseElementException;
import com.tippingpoint.database.Element;
import com.tippingpoint.database.Schema;
import com.tippingpoint.database.Table;
import com.tippingpoint.sql.ConnectionManager;
import com.tippingpoint.sql.ConnectionManagerFactory;
import com.tippingpoint.sql.SqlBaseException;
import com.tippingpoint.sql.SqlExecutionException;
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
	 * This method breaks down the string used to identify the object.
	 * 
	 * @param strObjects String containing the path information.
	 * @throws SQLException
	 * @throws DatabaseElementException
	 * @throws SqlExecutionException
	 */
	public static List<Element> getElements(final String strObjects) throws DatabaseElementException, SQLException,
			SqlExecutionException {
		final List<Element> listElements = new ArrayList<Element>();

		// convert the path string of type 'table/column' to an array of strings
		if (StringUtils.isNotBlank(strObjects)) {
			final List<String> listObjects = new ArrayList<String>();
			final StringTokenizer tokenizer = new StringTokenizer(strObjects, "/");
			while (tokenizer.hasMoreTokens()) {
				final String strObject = StringUtils.trimToNull(tokenizer.nextToken());
				if (strObject != null) {
					listObjects.add(strObject);
				}
			}

			// if strings were specified, then convert to elements
			if (listObjects.size() > 0) {
				final ConnectionManager manager = ConnectionManagerFactory.getFactory().getDefaultManager();
				final Schema schema = manager.getSchema(manager.getConnectionSource().getSchema());

				final Table table = schema.getTable(listObjects.get(0));
				if (table != null) {
					listElements.add(table);

					// if there are more objects, then find the column or constraint
					if (listObjects.size() > 1) {
						final ColumnDefinition column = table.getColumn(listObjects.get(1));
						if (column != null) {
							listElements.add(column);
						}
						else {
							final Constraint constraint = table.getConstraint(listObjects.get(1));
							if (constraint != null) {
								listElements.add(constraint);
							}
						}
					}
				}
			}
		}

		return listElements;
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
	 * This method returns the staff object by id.
	 * 
	 * @throws SqlBaseException
	 */
	@GET
	@Path("/{id:[a-f0-9\\-]+}")
	@Produces(MediaType.APPLICATION_XML)
	public StreamingOutput getXmlObjectById(@PathParam("id") final String strId) throws SqlBaseException {
		final BusinessObject businessObject = getObjectById(strId);
		StreamingOutput output = null;
		if (businessObject != null)
			output = new ServiceOutputObjectXml(businessObject, false);
		
		return output;
	}
	
	/**
	 * This class is used generate the output of the business object. The specific format is deferred to child classes.
	 */
	protected class ServiceOutputObjectXml implements StreamingOutput {
		protected BusinessObject m_object;
		protected boolean m_bDeep;

		/**
		 * This method constructs the stream object used for rendering web pages.
		 * 
		 * @param strObjetName
		 */
		public ServiceOutputObjectXml(final BusinessObject object, boolean bDeep) {
			m_object = object;
			m_bDeep = bDeep;
		}

		@Override
		public void write(final OutputStream writer) throws IOException {
			if (m_object != null) {
				final List<NameValuePair> listAttributes = new ArrayList<NameValuePair>();

				listAttributes.add(new NameValuePair(XmlTags.ATTRIBUTE_NAME, m_object.getType()));

				final FieldValue fvIdentifier = m_object.getIdentifierField();
				if (fvIdentifier != null) {
					listAttributes.add(new NameValuePair(fvIdentifier.getName(), XmlUtilities.getValue(fvIdentifier
							.getValue())));
				}

				writer.write(XmlUtilities.open(XmlTags.TAG_OBJECT, listAttributes).getBytes());

				final Iterator<FieldValue> iterValues = m_object.getValues();
				if (iterValues != null && iterValues.hasNext()) {
					while (iterValues.hasNext()) {
						final FieldValue fieldValue = iterValues.next();
						if (fvIdentifier == null || !fieldValue.getName().equals(fvIdentifier.getName())) {
							writer.write(XmlUtilities.tag(XmlTags.TAG_FIELD,
									new NameValuePair(XmlTags.ATTRIBUTE_NAME, fieldValue.getName()),
									XmlUtilities.getValue(fieldValue.getValue())).getBytes());
						}
					}
				}

				if (m_bDeep) {
					final List<String> listRelatedNames = m_object.getRelatedNames();
					if (listRelatedNames != null && !listRelatedNames.isEmpty()) {
						for (final String strRelatedName : listRelatedNames) {
							try {
								final List<BusinessObject> listRelatedObjects =
									m_object.getReleatedObjects(strRelatedName);
								if (listRelatedObjects != null && !listRelatedObjects.isEmpty()) {
									writer.write(XmlUtilities.open(XmlTags.TAG_LIST, new NameValuePair(XmlTags.ATTRIBUTE_NAME, strRelatedName)).getBytes());
									for (final BusinessObject businessRelatedObject : listRelatedObjects) {
										write(writer);
									}
	
									writer.write(XmlUtilities.close(XmlTags.TAG_LIST).getBytes());
								}
							}
							catch (final SqlBaseException e) {
								throw new IllegalStateException(e);
							}

						}
					}
				}

				writer.write(XmlUtilities.close(XmlTags.TAG_OBJECT).getBytes());
			}
			else {
				writer.write(XmlUtilities.tag(XmlTags.TAG_OBJECT).getBytes());
			}
		}

	}

	/**
	 * This method writes the business object to the writer.
	 * 
	 * @param writer Writer used for writing out the XML.
	 * @param businessObject BusinessObject which is being written to the XML.
	 * @param bDeep boolean indicating if related objects should be retrieved at the same time.
	 * @throws IOException
	 * @throws SqlBaseException
	 */
	protected void writeObject(final Writer writer, final BusinessObject businessObject, final boolean bDeep)
			throws IOException, SqlBaseException {
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
