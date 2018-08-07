package com.marklogic.migration.rdbms;

import com.marklogic.client.document.DocumentWriteOperation;
import com.marklogic.client.ext.batch.BatchWriter;
import com.marklogic.client.ext.helper.LoggingObject;
import com.marklogic.client.impl.DocumentWriteOperationImpl;
import com.marklogic.client.io.DocumentMetadataHandle;
import com.marklogic.client.io.Format;
import com.marklogic.client.io.StringHandle;
import com.marklogic.spring.batch.columnmap.ColumnMapSerializer;
import com.marklogic.spring.batch.columnmap.JacksonColumnMapSerializer;
import com.marklogic.spring.batch.item.processor.support.UriGenerator;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * TODO Multi-thread this later.
 */
public class TableQueryWriter extends LoggingObject implements ItemWriter<Map<String, Object>>, ItemStream {

	private TableQuery tableQuery;
	private BatchWriter batchWriter;
	private JdbcTemplate jdbcTemplate;

	// TODO Make these configurable
	private ChildQueryExecutor childQueryExecutor = new ChildQueryExecutor();
	private ColumnMapSerializer columnMapSerializer = new JacksonColumnMapSerializer();
	private UriGenerator<Map<String, Object>> uriGenerator;

	public TableQueryWriter(TableQuery tableQuery, BatchWriter batchWriter, JdbcTemplate jdbcTemplate) {
		this.tableQuery = tableQuery;
		this.batchWriter = batchWriter;
		this.jdbcTemplate = jdbcTemplate;

		this.uriGenerator = o -> {
			String uuid = UUID.randomUUID().toString();
			// TODO Need to know if JSON or XML
			return "/" + tableQuery.getElementName().replaceAll("[^A-Za-z0-9\\_\\-]", "") + "/" + uuid + ".json";
		};
	}

	@Override
	public void open(ExecutionContext executionContext) throws ItemStreamException {

	}

	@Override
	public void update(ExecutionContext executionContext) throws ItemStreamException {

	}

	@Override
	public void close() throws ItemStreamException {
		batchWriter.waitForCompletion();
	}

	@Override
	public void write(List<? extends Map<String, Object>> items) {
		jdbcTemplate.execute(new ConnectionCallback<Void>() {
			@Override
			public Void doInConnection(Connection con) {
				childQueryExecutor.executeChildQueries(con, tableQuery, items);
				return null;
			}
		});

		List<DocumentWriteOperation> documentWriteOperations = new ArrayList<>();
		for (Map<String, Object> item : items) {
			String content = columnMapSerializer.serializeColumnMap(item, tableQuery.getElementName());
			String uri;
			try {
				uri = uriGenerator.generateUri(item);
			} catch (Exception e) {
				throw new RuntimeException("Unable to generate URI for item: " + item, e);
			}
			DocumentWriteOperation op = new DocumentWriteOperationImpl(
				DocumentWriteOperation.OperationType.DOCUMENT_WRITE,
				uri,
				// TODO Customize metadata
				new DocumentMetadataHandle().withCollections("raw"),
				// TODO Make this configurable
				new StringHandle(content).withFormat(Format.JSON)
			);
			documentWriteOperations.add(op);
		}

		logger.info("Writing: " + documentWriteOperations.size());
		this.batchWriter.write(documentWriteOperations);
	}
}
