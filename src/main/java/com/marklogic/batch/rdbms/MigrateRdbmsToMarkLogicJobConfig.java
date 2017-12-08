package com.marklogic.batch.rdbms;

import com.marklogic.client.document.DocumentWriteOperation;
import com.marklogic.client.ext.helper.DatabaseClientProvider;
import com.marklogic.spring.batch.columnmap.ColumnMapSerializer;
import com.marklogic.spring.batch.columnmap.DefaultStaxColumnMapSerializer;
import com.marklogic.spring.batch.columnmap.JacksonColumnMapSerializer;
import com.marklogic.spring.batch.item.processor.ColumnMapProcessor;
import com.marklogic.spring.batch.item.rdbms.AllTablesItemReader;
import com.marklogic.spring.batch.item.writer.MarkLogicItemWriter;
import com.marklogic.spring.batch.item.writer.support.DefaultUriTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.ColumnMapRowMapper;

import java.util.Map;

@EnableBatchProcessing
@Import(value = {
        ApplicationProperties.class,
        com.marklogic.spring.batch.config.MarkLogicBatchConfiguration.class,
        com.marklogic.spring.batch.config.MarkLogicConfiguration.class})
@PropertySource(value = "classpath:application.properties", ignoreResourceNotFound = true)
@PropertySource(value = "file:application.properties", ignoreResourceNotFound = true)
public class MigrateRdbmsToMarkLogicJobConfig {

    protected final static Logger logger = LoggerFactory.getLogger(MigrateRdbmsToMarkLogicJobConfig.class);

    @Bean
    public Job job(JobBuilderFactory jobBuilderFactory, Step step) {
        return jobBuilderFactory.get("migrateRdbmsToMarkLogicJob")
                .incrementer(new RunIdIncrementer())
                .start(step)
                .build();
    }

    @Bean
    @JobScope
    public Step step(StepBuilderFactory stepBuilderFactory,
                     DatabaseClientProvider databaseClientProvider,
                     ApplicationProperties properties,
                     @Value("#{jobParameters['all_tables']}") String allTables,
                     @Value("#{jobParameters['xcc']}") String xcc,
                     @Value("#{jobParameters['collections']}") String collections,
                     @Value("#{jobParameters['permissions']}") String permissions,
                     @Value("#{jobParameters['hosts']}") String hosts,
                     @Value("#{jobParameters['thread_count'] ?: 4}") Integer threadCount,
                     @Value("#{jobParameters['chunk_size'] ?: 100}") Integer chunkSize,
                     @Value("#{jobParameters['sql']}") String sql,
                     @Value("#{jobParameters['root_local_name']}") String rootLocalName,
                     @Value("#{jobParameters['document_type']}") String documentType,
                     @Value("#{jobParameters['output_uri_prefix']}") String outputUriPrefix,
                     @Value("#{jobParameters['uri_id']}") String uriId) {

        ItemReader<Map<String, Object>> reader = null;
        if ("true".equals(allTables)) {
            // Use AllTablesReader to process rows from every table
            reader = new AllTablesItemReader(properties.getDataSource());
        } else {
            // Uses Spring Batch's JdbcCursorItemReader and Spring JDBC's ColumnMapRowMapper to map each row
            // to a Map<String, Object>. Normally, if you want more control, standard practice is to bind column values to
            // a POJO and perform any validation/transformation/etc you need to on that object.
            JdbcCursorItemReader<Map<String, Object>> r = new JdbcCursorItemReader<Map<String, Object>>();
            r.setRowMapper(new ColumnMapRowMapper());
            r.setDataSource(properties.getDataSource());
            r.setSql(sql);
            r.setRowMapper(new ColumnMapRowMapper());
            reader = r;
        }

        // Processor - this is a very basic implementation for converting a column map to an XML or JSON string
        ColumnMapSerializer serializer = null;
        if (documentType != null && documentType.toLowerCase().equals("json")) {
            serializer = new JacksonColumnMapSerializer();
        } else {
            serializer = new DefaultStaxColumnMapSerializer();
        }

        ColumnMapProcessor processor = new ColumnMapProcessor(serializer, new ColumnMapUriGenerator(uriId));
        if (rootLocalName != null) {
            processor.setRootLocalName(rootLocalName);
        }
        if (collections != null) {
            processor.setCollections(collections.split(","));
        }
        if (permissions != null) {
            processor.setPermissions(permissions.split(","));
        }

        DefaultUriTransformer uriTransformer = new DefaultUriTransformer();
        if (documentType != null && documentType.toLowerCase().equals("json")) {
            uriTransformer.setOutputUriSuffix(".json");
        } else {
            uriTransformer.setOutputUriSuffix(".xml");
        }
        uriTransformer.setOutputUriPrefix(outputUriPrefix);
        MarkLogicItemWriter writer = new MarkLogicItemWriter(databaseClientProvider.getDatabaseClient());
        writer.setUriTransformer(uriTransformer);
        writer.setThreadCount(threadCount);
        writer.setBatchSize(chunkSize);

        // Run the job!
        logger.info("Initialized components, launching job");
        return stepBuilderFactory.get("step1")
                .<Map<String, Object>, DocumentWriteOperation>chunk(chunkSize)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

}
