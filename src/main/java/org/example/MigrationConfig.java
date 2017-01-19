package org.example;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.document.DocumentWriteOperation;
import com.marklogic.client.helper.DatabaseClientConfig;
import com.marklogic.client.helper.LoggingObject;
import com.marklogic.spring.batch.Options;
import com.marklogic.spring.batch.columnmap.DefaultStaxColumnMapSerializer;
import com.marklogic.spring.batch.config.support.OptionParserConfigurer;
import com.marklogic.spring.batch.item.processor.ColumnMapProcessor;
import com.marklogic.spring.batch.item.writer.MarkLogicItemWriter;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@EnableBatchProcessing
public class MigrationConfig extends LoggingObject implements EnvironmentAware, OptionParserConfigurer {

	private Environment env;

	@Override
	public void configureOptionParser(OptionParser parser) {
		parser.accepts("collections", "Comma-delimited sequence of collections to insert each document into").withRequiredArg();
		parser.accepts("hosts", "Comma-delimited sequence of host names of MarkLogic nodes to write documents to").withRequiredArg();
		parser.accepts("permissions", "Comma-delimited sequence of permissions to apply to each document; role,capability,role,capability,etc").withRequiredArg();
		parser.accepts("rootLocalName", "Name of the root element in each document written to MarkLogic").withRequiredArg();
		parser.accepts("rootNamepaceUri", "Namespace URI of the root element in each document written to MarkLogic").withRequiredArg();
		parser.accepts("sql", "The SQL query for selecting rows to migrate").withRequiredArg();
		parser.accepts("threadCount", "The number of threads to use for writing to MarkLogic").withRequiredArg();
	}

	@Bean
	public Job job(JobBuilderFactory jobBuilderFactory, Step step) {
		return jobBuilderFactory.get("migrationJob").start(step).build();
	}

	@Bean
	@JobScope
	public Step step(StepBuilderFactory stepBuilderFactory,
	                 @Value("#{jobParameters['collections']}") String collections,
	                 @Value("#{jobParameters['permissions']}") String permissions,
	                 @Value("#{jobParameters['hosts']}") String hosts,
	                 @Value("#{jobParameters['threadCount']}") Integer threadCount,
	                 @Value("#{jobParameters['sql']}") String sql,
	                 @Value("#{jobParameters['rootLocalName']}") String rootLocalName) {

		// Determine the Spring Batch chunk size
		int chunkSize = 100;
		String prop = env.getProperty(Options.CHUNK_SIZE);
		if (prop != null) {
			chunkSize = Integer.parseInt(prop);
		}

		logger.info("Chunk size: " + env.getProperty(Options.CHUNK_SIZE));
		logger.info("Hosts: " + hosts);
		logger.info("SQL: " + sql);
		logger.info("Root local name: " + rootLocalName);
		logger.info("Collections: " + collections);
		logger.info("Permissions: " + permissions);
		logger.info("Thread count: " + threadCount);

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
		if (collections != null) {
			processor.setCollections(collections.split(","));
		}
		if (permissions != null) {
			processor.setPermissions(permissions.split(","));
		}

		// Writer
		List<DatabaseClient> databaseClients = buildDatabaseClients(hosts);
		MarkLogicItemWriter writer = new MarkLogicItemWriter(databaseClients);
		if (threadCount != null && threadCount > 0) {
			writer.setThreadCount(threadCount);
		}

		// Run the job
		return stepBuilderFactory.get("step1")
			.<Map<String, Object>, DocumentWriteOperation>chunk(chunkSize)
			.reader(reader)
			.processor(processor)
			.writer(writer)
			.build();
	}

	protected List<DatabaseClient> buildDatabaseClients(String hosts) {
		Integer port = Integer.parseInt(env.getProperty(Options.PORT));
		String username = env.getProperty(Options.USERNAME);
		String password = env.getProperty(Options.PASSWORD);
		String database = env.getProperty(Options.DATABASE);
		String auth = env.getProperty(Options.AUTHENTICATION);
		DatabaseClientFactory.Authentication authentication = DatabaseClientFactory.Authentication.DIGEST;
		if (auth != null) {
			authentication = DatabaseClientFactory.Authentication.valueOf(auth.toUpperCase());
		}

		logger.info("Client username: " + username);
		logger.info("Client database: " + database);
		logger.info("Client authentication: " + authentication.name());

		List<DatabaseClient> databaseClients = new ArrayList<>();
		if (hosts != null) {
			for (String host : hosts.split(",")) {
				logger.info("Creating client for host: " + host);
				databaseClients.add(DatabaseClientFactory.newClient(host, port, database, username, password, authentication));
			}
		} else {
			String host = env.getProperty(Options.HOST);
			logger.info("Creating client for host: " + host);
			databaseClients.add(DatabaseClientFactory.newClient(host, port, database, username, password, authentication));
		}

		return databaseClients;
	}

	protected DataSource buildDataSource() {
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
