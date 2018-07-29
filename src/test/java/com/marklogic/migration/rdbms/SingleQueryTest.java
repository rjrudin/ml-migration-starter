package com.marklogic.migration.rdbms;

import org.junit.Test;

public class SingleQueryTest extends AbstractTest {

	@Test
	public void test() throws Exception {
		String migrationJson = "{\"query\":\"select * from Customer where customer_id=1\"," +
			"\"primaryKeyColumnName\":\"customer_id\"," +
			"\"elementName\":\"customer\"," +
			"\"foreignKeyColumnName\":null," +
			"\"childQueries\":[]" +
			"}";

		runJob(migrationJson);
	}
}
