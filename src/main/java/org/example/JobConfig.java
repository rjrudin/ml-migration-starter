package org.example;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.document.DocumentWriteOperation;
import com.marklogic.spring.batch.Options;
import com.marklogic.spring.batch.columnmap.DefaultStaxColumnMapSerializer;
import com.marklogic.spring.batch.item.MarkLogicItemWriter;
import com.marklogic.spring.batch.item.processor.ColumnMapProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.Map;

@EnableBatchProcessing
public class JobConfig implements EnvironmentAware {

	private Environment env;

	@Bean
	public Job job(JobBuilderFactory jobBuilderFactory, Step step) {
		return jobBuilderFactory.get("job").start(step).build();
	}

	@Bean
	@JobScope
	public Step step(StepBuilderFactory stepBuilderFactory) {

		// TODO Externalize this
		String sql = "SELECT * FROM actor";

		JdbcCursorItemReader<Map<String, Object>> reader = new JdbcCursorItemReader<Map<String, Object>>();
		reader.setRowMapper(new ColumnMapRowMapper());
		reader.setDataSource(buildDataSource());
		reader.setSql(sql);
		reader.setRowMapper(new ColumnMapRowMapper());

		// TODO Parallelize
		DatabaseClient client = DatabaseClientFactory.newClient("localhost", 8000, "admin", "admin",
			DatabaseClientFactory.Authentication.DIGEST);
		MarkLogicItemWriter writer = new MarkLogicItemWriter(client);

		return stepBuilderFactory.get("step1")
			.<Map<String, Object>, DocumentWriteOperation>chunk(10)
			.reader(reader)
			.processor(new ColumnMapProcessor(new DefaultStaxColumnMapSerializer()))
			.writer(writer)
			.build();
	}

	protected DataSource buildDataSource() {
		//logger.info("Creating simple data source based on JDBC connection options");
		DriverManagerDataSource ds = new DriverManagerDataSource();
		ds.setDriverClassName(env.getProperty(Options.JDBC_DRIVER));
		ds.setUrl(env.getProperty(Options.JDBC_URL));
		ds.setUsername(env.getProperty(Options.JDBC_USERNAME));
		ds.setPassword(env.getProperty(Options.JDBC_PASSWORD));
		return ds;
	}

	@Override
	public void setEnvironment(Environment environment) {
		this.env = environment;
	}

}
