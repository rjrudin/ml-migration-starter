package com.marklogic.migration.rdbms;

import com.marklogic.junit.spring.AbstractSpringTest;
import com.marklogic.spring.batch.columnmap.ColumnMapSerializer;
import com.marklogic.spring.batch.columnmap.JacksonColumnMapSerializer;
import org.example.MigrationConfig;
import org.junit.Before;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.ContextConfiguration;

import java.util.HashMap;
import java.util.Map;

@ContextConfiguration(classes = {TestConfig.class, MigrationConfig.class})
public abstract class AbstractTest extends AbstractSpringTest {

	@Autowired
	protected JobLauncherTestUtils jobLauncherTestUtils;

	@Autowired
	protected Environment environment;

	protected JdbcTemplate jdbcTemplate;
	protected ColumnMapSerializer columnMapSerializer = new JacksonColumnMapSerializer();

	@Before
	public void setupJdbcTemplate() {
		DriverManagerDataSource ds = new DriverManagerDataSource(
			environment.getProperty("jdbc_url"),
			environment.getProperty("jdbc_username"),
			environment.getProperty("jdbc_password")
		);
		this.jdbcTemplate = new JdbcTemplate(ds);
	}

	protected void runJob(String migrationJson) {
		try {
			jobLauncherTestUtils.launchJob(new JobParameters(buildJobParameterMap(migrationJson)));
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	protected Map<String, JobParameter> buildJobParameterMap(String migrationJson) {
		Map<String, JobParameter> params = new HashMap<>();
		params.put("migration_json", new JobParameter(migrationJson));
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
