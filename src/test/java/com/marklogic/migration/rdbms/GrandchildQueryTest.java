package com.marklogic.migration.rdbms;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

public class GrandchildQueryTest extends AbstractTest {

	@Test
	public void test() throws Exception {
		TableQuery customerQuery = new TableQuery("select * from customer", "customer_id", null, "customer");
		TableQuery rentalQuery = new TableQuery("select * from rental", "rental_id", "customer_id", "rentals");
		customerQuery.addChildQuery(rentalQuery);
		TableQuery paymentQuery = new TableQuery("select * from payment", "payment_id", "rental_id", "payments");
		rentalQuery.addChildQuery(paymentQuery);

		String migrationJson = new ObjectMapper().writeValueAsString(customerQuery);
		System.out.println(migrationJson);
		runJob(migrationJson);
	}
}
