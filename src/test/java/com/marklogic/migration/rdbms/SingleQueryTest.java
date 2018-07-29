package com.marklogic.migration.rdbms;

import org.junit.Test;
import org.springframework.batch.core.JobParameters;

import java.util.Map;

public class SingleQueryTest extends AbstractTest {

	@Test
	public void test() throws Exception {
		TableQuery customerQuery = new TableQuery("select * from Customer where customer_id=1", "customer_id", null, "customer");
		Map<String, Object> map = jdbcTemplate.queryForMap(customerQuery.getQuery());
		System.out.println(map);

		jobLauncherTestUtils.launchJob(new JobParameters(buildJobParameterMap()));
	}
}
