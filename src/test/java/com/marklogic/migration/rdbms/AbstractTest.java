package com.marklogic.migration.rdbms;

import com.marklogic.junit.spring.AbstractSpringTest;
import com.marklogic.spring.batch.columnmap.ColumnMapSerializer;
import com.marklogic.spring.batch.columnmap.JacksonColumnMapSerializer;
import org.junit.Before;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {TestConfig.class})
public abstract class AbstractTest extends AbstractSpringTest {

	protected JdbcTemplate jdbcTemplate;
	protected ColumnMapSerializer columnMapSerializer = new JacksonColumnMapSerializer();

	// TODO Replace strings with properties
	@Before
	public void setupJdbcTemplate() {
		DriverManagerDataSource ds = new DriverManagerDataSource(
			"jdbc:mysql://localhost:3306/sakila?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC",
			"root", "password");
		this.jdbcTemplate = new JdbcTemplate(ds);
	}
}
