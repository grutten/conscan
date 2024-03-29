package com.tippingpoint.conscan.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.tippingpoint.database.Table;
import com.tippingpoint.sql.SqlBaseException;

/**
 * This class returns business object associated with database table.
 */
public class TableBusinessObjectBuilder implements BusinessObjectBuilder {
	/** This member holds the persistence mechanism used in the business object. */
	private final TablePersistence m_tablePersistence;

	/**
	 * This method constructs a new a builder for the object associated with the passed in table.
	 * 
	 * @param table Table for which the object should be created.
	 */
	public TableBusinessObjectBuilder(final Table table) {
		m_tablePersistence = new TablePersistence(table);
	}

	/**
	 * This method returns the name of the object being created. It must be unique in the system.
	 */
	@Override
	public BusinessObject get() {
		return new TableBusinessObject(m_tablePersistence);
	}

	/**
	 * This method is used to return a business object based on the passed in value. The business object determines what
	 * that value could be.
	 * 
	 * @param objId Object containing the identifying value.
	 * @throws SqlBaseException
	 */
	@Override
	public BusinessObject get(final Object objId) throws SqlBaseException {
		BusinessObject businessObject = null;
		final Map<String, FieldValue> mapValues = m_tablePersistence.get(objId);
		if (mapValues != null) {
			businessObject = get(mapValues);
		}

		return businessObject;
	}

	/**
	 * This method returns a collection of objects representing all of the objects of this type.
	 * 
	 * @throws SqlBaseException
	 */
	@Override
	public List<BusinessObject> getAll() throws SqlBaseException {
		return getAll(null);
	}

	/**
	 * This method returns a collection of objects representing all of the objects of this type with the given field
	 * values.
	 * 
	 * @param listCommonValues List containing the values that will be common to all the objects..
	 * @throws SqlBaseException
	 */
	@Override
	public List<BusinessObject> getAll(final List<FieldValue> listCommonValues) throws SqlBaseException {
		final List<BusinessObject> listObjects = new ArrayList<BusinessObject>();
		final List<Map<String, FieldValue>> listValues = m_tablePersistence.getAll(listCommonValues);
		if (listValues != null && !listValues.isEmpty()) {
			for (final Map<String, FieldValue> mapValues : listValues) {
				listObjects.add(get(mapValues));
			}
		}

		return listObjects;
	}

	/**
	 * This method returns a collection of objects representing all of the objects of this type; only the primary and
	 * logical keys will be returned.
	 * 
	 * @throws SqlBaseException
	 */
	public List<BusinessObject> getAllForReference() throws SqlBaseException {
		final List<BusinessObject> listObjects = new ArrayList<BusinessObject>();
		final List<Map<String, FieldValue>> listValues = m_tablePersistence.getAllForReference();
		if (listValues != null && !listValues.isEmpty()) {
			for (final Map<String, FieldValue> mapValues : listValues) {
				listObjects.add(get(mapValues));
			}
		}

		return listObjects;
	}

	/**
	 * This method returns a business object instance.
	 */
	@Override
	public String getName() {
		return m_tablePersistence.getTable().getName();
	}

	/**
	 * This method returns the name of the object referenced by the name of the field.
	 * 
	 * @param strColumnName String containing the name of a referenced field.
	 */
	public String getReferencedObjectType(String strColumnName) {
		String strReferencedObjectType = m_tablePersistence.getReferencedObjectType(strColumnName);

		return strReferencedObjectType;
	}

	/**
	 * This method returns a new object with the given values.
	 * 
	 * @param mapValues Map containing the default values of the object.
	 */
	private BusinessObject get(final Map<String, FieldValue> mapValues) {
		return new TableBusinessObject(m_tablePersistence, mapValues);
	}
}
