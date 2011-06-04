package com.tippingpoint.conscan.objects.json;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import junit.framework.Assert;
import org.junit.Test;
import com.tippingpoint.conscan.objects.BusinessObject;
import com.tippingpoint.conscan.objects.BusinessObjectImpl;
import com.tippingpoint.conscan.objects.FieldValue;
import com.tippingpoint.conscan.objects.Persistence;
import com.tippingpoint.database.Id;
import com.tippingpoint.sql.SqlBaseException;

/**
 * This class is used to test converting a business object to a JSON string.
 */
public class TestJsonBusinessObject {
	/**
	 * This class tests the basic attributes.
	 */
	@Test
	public void testBasics() {
		final TestPersistence persistence = new TestPersistence();
		Assert.assertNotNull(persistence);

		final TestBusinessObject objTest = new TestBusinessObject(persistence);
		Assert.assertNotNull(objTest);

		final JsonBusinessObject jsonBusinessObject = new JsonBusinessObject(objTest);
		Assert.assertNotNull(jsonBusinessObject);
		Assert.assertNotNull(jsonBusinessObject.get());
		Assert.assertEquals(
				"{\"field2\":\"bcd\",\"idvalue\":\"2222\",\"datevalue\":\"Tue Jan 13 12:38:31 PST 1970\",\"testid\":\"1234567890\",\"field1\":\"abc\",\"type\":\"test\"}",
				jsonBusinessObject.get().toJSONString());
	}

	/**
	 * This class is used to test business objects.
	 */
	private static class TestBusinessObject extends BusinessObjectImpl {
		public TestBusinessObject(final TestPersistence persistence) {
			super(persistence);

			super.setValue("testid", "1234567890");
			super.setValue("field1", "abc");
			super.setValue("field2", "bcd");
			super.setValue("datevalue", new Date(1111111111L));
			super.setValue("idvalue", new Id(2222));
		}

		@Override
		public String getType() {
			return "test";
		}
	}

	/**
	 * This class is used to test business objects.
	 */
	private static class TestPersistence implements Persistence {
		@Override
		public List<Map<String, FieldValue>> getAll(final List<FieldValue> listCommonValues) throws SqlBaseException {
			return null;
		}

		@Override
		public Iterator<String> getFields() {
			return null;
		}

		@Override
		public String getIdentifierName() {
			return "testid";
		}

		@Override
		public List<String> getRelatedNames() {
			return null;
		}

		@Override
		public List<BusinessObject> getReleatedObjects(final String strRelatedName,
				final Map<String, FieldValue> mapValues) throws SqlBaseException {
			return null;
		}

		@Override
		public void save(final Map<String, FieldValue> mapValues) throws SqlBaseException {
			// don't save anything since this is just simulating persistence
		}

		@Override
		public void delete(Map<String, FieldValue> m_mapValues) {
			// TODO Auto-generated method stub
			
		}
	}
}
