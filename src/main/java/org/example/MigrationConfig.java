package org.example;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.document.DocumentWriteOperation;
import com.marklogic.client.helper.LoggingObject;
import com.marklogic.spring.batch.Options;
import com.marklogic.spring.batch.columnmap.DefaultStaxColumnMapSerializer;
import com.marklogic.spring.batch.config.support.OptionParserConfigurer;
import com.marklogic.spring.batch.item.MarkLogicItemWriter;
import com.marklogic.spring.batch.item.processor.ColumnMapProcessor;
import joptsimple.OptionParser;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.Map;

@EnableBatchProcessing
public class MigrationConfig extends LoggingObject implements EnvironmentAware, OptionParserConfigurer {

	private Environment env;

	@Override
	public void configureOptionParser(OptionParser parser) {
		parser.accepts("sql", "The SQL query for selecting rows to migrate").withRequiredArg();
		parser.accepts("rootLocalName", "Name of the root element in each document written to MarkLogic").withRequiredArg();
		parser.accepts("rootNamepaceUri", "Namespace URI of the root element in each document written to MarkLogic").withRequiredArg();
	}

	@Bean
	public Job job(JobBuilderFactory jobBuilderFactory, Step step) {
		return jobBuilderFactory.get("job").start(step).build();
	}

	@Bean
	@JobScope
	public Step step(StepBuilderFactory stepBuilderFactory,
	                 @Value("#{jobParameters['sql']}") String sql,
	                 @Value("#{jobParameters['rootLocalName']}") String rootLocalName) {

		logger.info("SQL: " + sql);
		logger.info("rootLocalName: " + rootLocalName);

		// Reader
		JdbcCursorItemReader<Map<String, Object>> reader = new JdbcCursorItemReader<Map<String, Object>>();
		reader.setRowMapper(new ColumnMapRowMapper());
		reader.setDataSource(buildDataSource());
		reader.setSql(sql);
		reader.setRowMapper(new ColumnMapRowMapper());

		// Processor
		ColumnMapProcessor processor = new ColumnMapProcessor(new DefaultStaxColumnMapSerializer());
		if (rootLocalName != null) {
			processor.setRootLocalName(rootLocalName);
		}

		// Writer TODO Parallelize
		DatabaseClient client = DatabaseClientFactory.newClient("localhost", 8000, "admin", "admin",
			DatabaseClientFactory.Authentication.DIGEST);
		MarkLogicItemWriter writer = new MarkLogicItemWriter(client);

		// Run the job
		return stepBuilderFactory.get("step1")
			.<Map<String, Object>, DocumentWriteOperation>chunk(100)
			.reader(reader)
			.processor(processor)
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
