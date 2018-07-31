package com.marklogic.migration.rdbms;

import org.junit.Test;

public class SingleQueryTest extends AbstractTest {

	@Test
	public void test() {
		String migrationJson = "{\"query\":\"select * from customer where customer_id=1\"," +
			"\"primaryKeyColumnName\":\"customer_id\"," +
			"\"elementName\":\"customer\"," +
			"\"foreignKeyColumnName\":null," +
			"\"childQueries\":[]" +
			"}";

		runJob(migrationJson);
	}
}
