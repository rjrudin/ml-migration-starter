package com.marklogic.rdbms.migration;

import com.marklogic.rdbms.migration.TableQuery;
import com.marklogic.spring.batch.columnmap.ColumnMapSerializer;
import com.marklogic.spring.batch.columnmap.JacksonColumnMapSerializer;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TableQueryMain {

	private static JdbcTemplate jdbcTemplate;
	private static ColumnMapSerializer columnMapSerializer = new JacksonColumnMapSerializer();
	private static ColumnMapRowMapper columnMapRowMapper = new ColumnMapRowMapper();

	/**
	 * So the query in TableQuery is used to populate the Reader. Processor can do whatever. The Writer then gets a
	 * chunk of column maps and hands those off to a thread. The thread then processes each child query to fully
	 * populate each column map, then serializes each one to JSON to write it off.
	 * @param args
	 */
	public static void main(String[] args) {
		DriverManagerDataSource ds = new DriverManagerDataSource(
			"jdbc:mysql://localhost:3306/sakila?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC",
			"root", "password");

		jdbcTemplate = new JdbcTemplate(ds);

		TableQuery tableQuery = new TableQuery("select * from Customer limit 100", "customer_id", null, "customer");
		tableQuery.addChildQuery(new TableQuery("select * from Payment", "payment_id", "customer_id", "payments"));

		List<Map<String, Object>> rows = process(tableQuery);
		System.out.println(columnMapSerializer.serializeColumnMap(rows.get(0), tableQuery.getElementName()));
	}

	private static List<Map<String, Object>> process(TableQuery tableQuery) {
		List<Map<String, Object>> parentRows = jdbcTemplate.query(tableQuery.getQuery(), columnMapRowMapper);

		for (TableQuery childTableQuery : tableQuery.getChildQueries()) {
			// Construct a map based on primary key so we can easily get the primary keys and populate the maps with kids later
			Map<Object, Map<String, Object>> parentMap = new LinkedHashMap<>();
			for (Map<String, Object> parentRow : parentRows) {
				Object parentId = parentRow.get(tableQuery.getPrimaryKeyColumnName());
				parentMap.put(parentId, parentRow);
			}

			// TODO Need to know the primary key column for the parent query
			// TODO Will need a column for the join column
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

		return parentRows;
	}
}
