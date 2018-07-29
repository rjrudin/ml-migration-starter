package com.marklogic.rdbms.migration;

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
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;

/**
 * TODO Multi-thread this later.
 * <p>
 * TODO Add callback interface for the list after child queries have been applied.
 */
public class TableQueryWriter extends LoggingObject implements ItemWriter<Map<String, Object>>, ItemStream {

	private TableQuery tableQuery;
	private BatchWriter batchWriter;
	private JdbcTemplate jdbcTemplate;

	// TODO Make these configurable
	private ColumnMapSerializer columnMapSerializer = new JacksonColumnMapSerializer();
	private ColumnMapRowMapper columnMapRowMapper = new ColumnMapRowMapper();
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
		logger.info("Waiting for completion");
		batchWriter.waitForCompletion();
		logger.info("Done!");
	}

	@Override
	public void write(List<? extends Map<String, Object>> items) {
		applyChildQueries(items);

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

	private void applyChildQueries(List<? extends Map<String, Object>> parentRows) {
		for (TableQuery childTableQuery : tableQuery.getChildQueries()) {
			// Construct a map based on primary key so we can easily get the primary keys and populate the maps with kids later
			Map<Object, Map<String, Object>> parentMap = new LinkedHashMap<>();
			for (Map<String, Object> parentRow : parentRows) {
				Object parentId = parentRow.get(tableQuery.getPrimaryKeyColumnName());
				parentMap.put(parentId, parentRow);
			}

			StringBuilder childInClause = new StringBuilder(childTableQuery.getForeignKeyColumnName() + " IN (");
			boolean firstOne = true;
			for (Object parentId : parentMap.keySet()) {
				if (!firstOne) {
					childInClause.append(",");
				}
				// TODO Need to know whether to quote this or not
				childInClause.append(parentId);
				firstOne = false;
			}
			childInClause.append(")");

			// This is provided by user; can contain a where clause
			String childQuery = childTableQuery.getQuery();
			String lowerCaseQuery = childQuery.toLowerCase();
			if (!lowerCaseQuery.contains(" where ")) {
				childQuery += " WHERE ";
			} else {
				childQuery += " AND ";
			}
			childQuery += childInClause;

			List<Map<String, Object>> childRows = jdbcTemplate.query(childQuery, columnMapRowMapper);

			// Now add each child map to the correct parent map
			// Note that for one-many relationships, there's no column in the parent object
			// TODO many-to-one are different, there is a column that we may want to replace, but can always transform it away
			for (Map<String, Object> childRow : childRows) {
				Object parentId = childRow.get(childTableQuery.getForeignKeyColumnName());
				Map<String, Object> parentRow = parentMap.get(parentId);
				List<Map<String, Object>> kids;
				final String childElementName = childTableQuery.getElementName();
				if (parentRow.containsKey(childElementName)) {
					kids = (List<Map<String, Object>>) parentRow.get(childElementName);
				} else {
					kids = new ArrayList<>();
					parentRow.put(childElementName, kids);
				}
				// TODO Remove the foreign key from the child row?
				kids.add(childRow);
			}
		}
	}
}
