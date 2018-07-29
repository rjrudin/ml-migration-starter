package com.marklogic.migration.rdbms;

import org.junit.Test;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;

public class SingleQueryTest extends AbstractTest {

	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;

	@Autowired
	private Environment environment;

	@Test
	public void test() throws Exception {
		TableQuery customerQuery = new TableQuery("select * from Customer where customer_id=1", "customer_id", null, "customer");
		Map<String, Object> map = jdbcTemplate.queryForMap(customerQuery.getQuery());
		System.out.println(map);

		jobLauncherTestUtils.launchJob(new JobParameters(buildJobParameterMap()));
	}

	protected Map<String, JobParameter> buildJobParameterMap() {
		Map<String, JobParameter> params = new HashMap<>();
		params.put("host", new JobParameter(environment.getProperty("mlHost")));
		params.put("port", new JobParameter(Long.parseLong(environment.getProperty("mlTestRestPort"))));
		params.put("username", new JobParameter(environment.getProperty("mlUsername")));
		params.put("password", new JobParameter(environment.getProperty("mlPassword")));
		addStringProperty(params, "jdbc_driver");
		addStringProperty(params, "jdbc_url");
		addStringProperty(params, "jdbc_username");
		addStringProperty(params, "jdbc_password");
		params.put("chunk_size", new JobParameter(Long.parseLong(environment.getProperty("chunk_size"))));
		return params;
	}

	protected void addStringProperty(Map<String, JobParameter> params, String name) {
		params.put(name, new JobParameter(environment.getProperty(name)));
	}
}
