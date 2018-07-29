package org.example;

import com.marklogic.spring.batch.columnmap.ColumnMapSerializer;
import com.marklogic.spring.batch.columnmap.JacksonColumnMapSerializer;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ChildMain {

	public static void main(String[] args) throws Exception {

		DriverManagerDataSource ds = new DriverManagerDataSource(
			"jdbc:mysql://localhost:3306/sakila?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC",
			"root", "password");

		JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
		ColumnMapSerializer columnMapSerializer = new JacksonColumnMapSerializer();

		List<Map<String, Object>> parentRows = jdbcTemplate.query("select * from Customer limit 100", new ColumnMapRowMapper());

		final String parentPrimaryKeyName = "customer_id";
		final String childForeignKeyName = "customer_id";
		final String childElementName = "payments";

		// Construct a map based on primary key so we can easily get the primary keys and populate the maps
		// with kids later
		Map<Object, Map<String, Object>> parentMap = new LinkedHashMap<>();
		for (Map<String, Object> parentRow : parentRows) {
			Object parentId = parentRow.get(parentPrimaryKeyName);
			parentMap.put(parentId, parentRow);
		}

		// TODO Need to know the primary key column for the parent query
		// TODO Will need a column for the join column
		StringBuilder childInClause = new StringBuilder(childForeignKeyName + " IN (");
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
		String childQuery = "select * from Payment";
		String lowerCaseQuery = childQuery.toLowerCase();
		if (!lowerCaseQuery.contains(" where ")) {
			childQuery += " WHERE ";
		} else {
			childQuery += " AND ";
		}
		childQuery += childInClause;

		List<Map<String, Object>> childRows = jdbcTemplate.query(childQuery, new ColumnMapRowMapper());

		// Now add the map to each parent map
		// TODO Will need to know the name of the field
		// Note that for one-many relationships, there's no column in the parent object
		// TODO many-to-one are different, there is a column that we may want to replace, but can always transform it away
		for (Map<String, Object> childRow : childRows) {
			Object parentId = childRow.get(childForeignKeyName);
			Map<String, Object> parentRow = parentMap.get(parentId);
			List<Map<String, Object>> kids;
			if (parentRow.containsKey(childElementName)) {
				kids = (List<Map<String, Object>>)parentRow.get(childElementName);
			} else {
				kids = new ArrayList<Map<String, Object>>();
				parentRow.put(childElementName, kids);
			}
			// TODO Remove the foreign key from the child row?
			kids.add(childRow);
		}

		Map<String, Object> parentRow = parentRows.get(0);
		System.out.println(columnMapSerializer.serializeColumnMap(parentRow, "customer"));
	}
}
