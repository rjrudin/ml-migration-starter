package com.marklogic.migration.rdbms;

import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ChildQueryExecutor {

	private RowMapper<Map<String, Object>> columnMapRowMapper;

	public ChildQueryExecutor() {
		this(new ColumnMapRowMapper());
	}

	public ChildQueryExecutor(RowMapper<Map<String, Object>> columnMapRowMapper) {
		this.columnMapRowMapper = columnMapRowMapper;
	}

	/**
	 * @param connection
	 * @param parentTableQuery
	 * @param parentRows
	 */
	public void executeChildQueries(Connection connection, TableQuery parentTableQuery, List<? extends Map<String, Object>> parentRows) {
		// Construct a map based on primary key so we can easily get the primary keys and populate the maps with kids later
		Map<Object, Map<String, Object>> parentMap = new LinkedHashMap<>();
		for (Map<String, Object> parentRow : parentRows) {
			Object parentId = parentRow.get(parentTableQuery.getPrimaryKeyColumnName());
			parentMap.put(parentId, parentRow);
		}

		for (TableQuery childTableQuery : parentTableQuery.getChildQueries()) {
			// This is provided by user; can contain a where clause
			String childQuery = constructQuery(childTableQuery, parentMap);

			//logger.info("Child query: " + childQuery);
			List<Map<String, Object>> childRows = executeChildQuery(connection, childQuery);

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
				kids.add(childRow);
			}

			executeChildQueries(connection, childTableQuery, childRows);
		}
	}

	protected String constructQuery(TableQuery childTableQuery, Map<Object, Map<String, Object>> parentMap) {
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
		return childQuery;
	}

	protected List<Map<String, Object>> executeChildQuery(Connection connection, String childQuery) {
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			preparedStatement = connection.prepareStatement(childQuery);
			resultSet = preparedStatement.executeQuery();
			List<Map<String, Object>> childRows = new ArrayList<>();
			while (resultSet.next()) {
				childRows.add(columnMapRowMapper.mapRow(resultSet, 0));
			}
			return childRows;
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
		} finally {
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (SQLException e) {
					// ignore
				}
			}
			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					// ignore
				}
			}
		}
	}

}
