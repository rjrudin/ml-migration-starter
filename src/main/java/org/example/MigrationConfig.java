package org.example;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.document.DocumentWriteOperation;
import com.marklogic.client.ext.batch.RestBatchWriter;
import com.marklogic.rdbms.migration.TableQuery;
import com.marklogic.rdbms.migration.TableQueryWriter;
import com.marklogic.spring.batch.columnmap.ColumnMapSerializer;
import com.marklogic.spring.batch.columnmap.DefaultStaxColumnMapSerializer;
import com.marklogic.spring.batch.columnmap.JacksonColumnMapSerializer;
import com.marklogic.spring.batch.item.processor.ColumnMapProcessor;
import com.marklogic.spring.batch.item.rdbms.AllTablesItemReader;
import com.marklogic.spring.batch.item.writer.MarkLogicItemWriter;
import com.marklogic.spring.batch.item.writer.support.DefaultUriTransformer;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.Map;

/**
 * Spring Batch Configuration class that defines the Spring Batch job and step components, along with the job properties
 * for the step, which does all the work.
 */
@EnableBatchProcessing
public class MigrationConfig {

	/**
	 * Defines the job for Spring Batch to run. This job consists of a single step, defined below.
	 */
	@Bean
	public Job job(JobBuilderFactory jobBuilderFactory, Step step) {
		return jobBuilderFactory.get("migrateJob")
			.start(step)
			.build();
	}

	/**
	 * Defines the single step in the job, along with all of the job parameters for the migration process.
	 */
	@Bean
	@JobScope
	public Step step(StepBuilderFactory stepBuilderFactory,
	                 @Value("#{jobParameters['host']}") String host,
	                 @Value("#{jobParameters['port']}") Integer port,
	                 @Value("#{jobParameters['username']}") String username,
	                 @Value("#{jobParameters['password']}") String password,
	                 @Value("#{jobParameters['jdbc_driver']}") String jdbcDriver,
	                 @Value("#{jobParameters['jdbc_url']}") String jdbcUrl,
	                 @Value("#{jobParameters['jdbc_username']}") String jdbcUsername,
	                 @Value("#{jobParameters['jdbc_password']}") String jdbcPassword,
	                 @Value("#{jobParameters['all_tables']}") String allTables,
	                 @Value("#{jobParameters['chunk_size']}") Integer chunkSize,
	                 @Value("#{jobParameters['collections']}") String collections,
	                 @Value("#{jobParameters['document_type']}") String documentType,
	                 @Value("#{jobParameters['output_uri_prefix']}") String outputUriPrefix,
	                 @Value("#{jobParameters['permissions']}") String permissions,
	                 @Value("#{jobParameters['root_local_name']}") String rootLocalName,
	                 @Value("#{jobParameters['sql']}") String sql,
	                 @Value("#{jobParameters['thread_count']}") Integer threadCount,
	                 @Value("#{jobParameters['uri_id']}") String uriId) {

		// TODO Build this from configuration
		TableQuery customerQuery = new TableQuery("select * from Customer", "customer_id", null, "customer");
		TableQuery rentalQuery = new TableQuery("select * from Rental", "rental_id", "customer_id", "rentals");
		customerQuery.addChildQuery(rentalQuery);
		TableQuery paymentQuery = new TableQuery("select * from Payment", "payment_id", "rental_id", "payments");
		rentalQuery.addChildQuery(paymentQuery);

		// Construct a simple DataSource that Spring Batch will use to connect to an RDBMS
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(jdbcDriver);
		dataSource.setUrl(jdbcUrl);
		dataSource.setUsername(jdbcUsername);
		dataSource.setPassword(jdbcPassword);

		// Construct a Spring Batch ItemReader that can read rows from an RDBMS
		ItemReader<Map<String, Object>> reader = null;
		if ("true".equals(allTables)) {
			// Use AllTablesReader to process rows from every table
			reader = new AllTablesItemReader(dataSource);
		} else {
			// Uses Spring Batch's JdbcCursorItemReader and Spring JDBC's ColumnMapRowMapper to map each row
			// to a Map<String, Object>. Normally, if you want more control, standard practice is to bind column values to
			// a POJO and perform any validation/transformation/etc you need to on that object.
			JdbcCursorItemReader<Map<String, Object>> r = new JdbcCursorItemReader<Map<String, Object>>();
			r.setRowMapper(new ColumnMapRowMapper());
			r.setDataSource(dataSource);
			r.setSql(customerQuery.getQuery());
			reader = r;
		}

		// marklogic-spring-batch component that is used to write a Spring ColumnMap to an XML or JSON document
		ColumnMapSerializer serializer = null;
		if (documentType != null && documentType.toLowerCase().equals("json")) {
			serializer = new JacksonColumnMapSerializer();
		} else {
			serializer = new DefaultStaxColumnMapSerializer();
		}

		// marklogic-spring-batch component for converting a Spring ColumnMap into an XML or JSON document
		// that can be written to MarkLogic
//		ColumnMapProcessor processor = new ColumnMapProcessor(serializer, new ColumnMapUriGenerator(uriId));
//		if (rootLocalName != null) {
//			processor.setRootLocalName(rootLocalName);
//		}
//		if (collections != null) {
//			processor.setCollections(collections.split(","));
//		}
//		if (permissions != null) {
//			processor.setPermissions(permissions.split(","));
//		}

		// marklogic-spring-batch component for generating a URI for a document
//		DefaultUriTransformer uriTransformer = new DefaultUriTransformer();
//		if (documentType != null && documentType.toLowerCase().equals("json")) {
//			uriTransformer.setOutputUriSuffix(".json");
//		} else {
//			uriTransformer.setOutputUriSuffix(".xml");
//		}
//		uriTransformer.setOutputUriPrefix(outputUriPrefix);

		// Construct a DatabaseClient to connect to MarkLogic. Additional command line arguments can be added to
		// further customize this.
		DatabaseClient client = DatabaseClientFactory.newClient(host, port, new DatabaseClientFactory.DigestAuthContext(username, password));

		// Spring Batch ItemWriter for writing documents to MarkLogic
//		MarkLogicItemWriter writer = new MarkLogicItemWriter(client);
//		writer.setUriTransformer(uriTransformer);
//		writer.setThreadCount(threadCount);
//		writer.setBatchSize(chunkSize);

		TableQueryWriter writer = new TableQueryWriter(customerQuery, new RestBatchWriter(client), new JdbcTemplate(dataSource));

		// Return a step with the reader, processor, and writer constructed above.
		return stepBuilderFactory.get("step1")
			.<Map<String, Object>, Map<String, Object>>chunk(chunkSize)
			.reader(reader)
			//.processor(processor)
			.writer(writer)
			.build();
	}

}
