package com.marklogic.spring.batch.item.reader.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.support.AbstractItemStreamItemReader;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * TODO Add this to marklogic-spring-batch
 *
 * Spring Batch Reader that first queries for all the table names from the given DataSource, and then reads rows from
 * every table.
 *
 * The excludeTableNames property can be used to exclude certain table names from processing.
 *
 * The tableQueries property can be used to specify a custom SELECT query for a particular table name. By default,
 * "SELECT * FROM (table name)" is used.
 */
public class AllTablesReader extends AbstractItemStreamItemReader<Map<String, Object>> {

	public final static String DEFAULT_TABLE_NAME_KEY = "_tableName";

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private DataSource dataSource;
	private List<String> tableNames;
	private Map<String, JdbcCursorItemReader> tableReaders;
	private int tableNameIndex = 0;
	private String tableNameKey = DEFAULT_TABLE_NAME_KEY;

	// For ignoring certain table names
	private Set<String> excludeTableNames;

	// For using a custom SQL query for a given table name
	private Map<String, String> tableQueries;

	public AllTablesReader(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Use the DataSource to get a list of all the tables. Then, create a JdbcCursorItemReader for every table with
	 * a SQL query of "SELECT * FROM (table)". Put each of those into a List. Set a RowColumnRowMapper on each
	 * JdbcCursorItemReader so that every row is read as a ColumnMap.
	 */
	@Override
	public void open(ExecutionContext executionContext) throws ItemStreamException {
		tableNames = getTableNames();
		tableReaders = new HashMap<>();
		for (String tableName : tableNames) {
			tableReaders.put(tableName, buildTableReader(tableName, executionContext));
		}
	}

	/**
	 * Reads a row from the active JdbcCursorItemReader. If that returns null, move on to the next JdbcCursorItemReader.
	 * If there are no more readers, then this method is all done.
	 */
	@Override
	public Map<String, Object> read() throws Exception {
		final String currentTableName = tableNames.get(tableNameIndex);
		JdbcCursorItemReader<Map<String, Object>> reader = tableReaders.get(currentTableName);
		Map<String, Object> result = reader.read();
		if (result != null) {
			result.put("_tableName", currentTableName);
			return result;
		}

		if (logger.isInfoEnabled()) {
			logger.info("Finished reading rows for query: " + reader.getSql());
		}
		reader.close();

		// Bump up index - if we're at the end of the list, we're all done
		tableNameIndex++;
		if (tableNameIndex >= tableNames.size()) {
			return null;
		}

		return read();
	}

	/**
	 * Register a custom SQL query for selecting data from the given table name. By default, a query of
	 * "SELECT * FROM (table name)" is used.
	 *
	 * @param tableName
	 * @param sql
	 */
	public void addTableQuery(String tableName, String sql) {
		if (tableQueries == null) {
			tableQueries = new HashMap<>();
		}
		tableQueries.put(tableName, sql);
	}

	/**
	 * @return a list of the table names, retrieved via the connection metadata object. The excludeTableNames
	 * property can be used to ignore certain table names.
	 */
	protected List<String> getTableNames() {
		return new JdbcTemplate(dataSource).execute(new ConnectionCallback<List<String>>() {
			@Override
			public List<String> doInConnection(Connection con) throws SQLException, DataAccessException {
				ResultSet rs = con.getMetaData().getTables(null, null, "%", null);
				List<String> list = new ArrayList<>();
				while (rs.next()) {
					String name = rs.getString("TABLE_NAME");
					if (excludeTableNames == null || !excludeTableNames.contains(name)) {
						list.add(name);
					}
				}
				return list;
			}
		});
	}

	/**
	 * @param tableName
	 * @param executionContext
	 * @return a JdbcCursorItemReader for the given table name. Override this method to alert the SQL statement that's
	 * used for a particular table.
	 */
	protected JdbcCursorItemReader<Map<String, Object>> buildTableReader(String tableName, ExecutionContext executionContext) {
		JdbcCursorItemReader<Map<String, Object>> reader = new JdbcCursorItemReader<>();
		reader.setDataSource(dataSource);
		reader.setRowMapper(new ColumnMapRowMapper());
		reader.setSql(getSqlQueryForTable(tableName));
		reader.open(executionContext);
		return reader;
	}

	/**
	 * Uses the tableQueries property to see if there's a custom SQL query for the given table name.
	 *
	 * @param tableName
	 * @return
	 */
	protected String getSqlQueryForTable(String tableName) {
		String sql = null;
		if (tableQueries != null) {
			sql = tableQueries.get(tableName);
		}
		return sql != null ? sql : "SELECT * FROM " + tableName;
	}

	public void setExcludeTableNames(Set<String> excludeTableNames) {
		this.excludeTableNames = excludeTableNames;
	}

	public void setTableQueries(Map<String, String> tableQueries) {
		this.tableQueries = tableQueries;
	}

	public void setTableNameKey(String tableNameKey) {
		this.tableNameKey = tableNameKey;
	}
}
