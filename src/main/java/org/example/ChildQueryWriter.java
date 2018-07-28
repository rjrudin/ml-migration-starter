package org.example;

import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * <Invoice>
 * <ID>33</ID>
 * <CUSTOMERID>40</CUSTOMERID>
 * <TOTAL>4300.20</TOTAL>
 * <INVOICEID>33</INVOICEID>
 * <ITEM>2</ITEM>
 * <PRODUCTID>36</PRODUCTID>
 * <QUANTITY>16</QUANTITY>
 * <COST>4.80</COST>
 * <STUFFID>2</STUFFID>
 * <COLOR>Red</COLOR>
 * <SIZE>Large</SIZE>
 * </Invoice>
 */
public class ChildQueryWriter implements ItemWriter<Map<String, Object>> {

	private List<ChildQuery> childQueries;
	private JdbcTemplate jdbcTemplate;

	// TODO Will need to support multiple column names
	private String primaryKeyColumnName;

	public ChildQueryWriter(JdbcTemplate jdbcTemplate, String primaryKeyColumnName, ChildQuery... childQueries) {
		this.jdbcTemplate = jdbcTemplate;
		this.childQueries = Arrays.asList(childQueries);
	}

	/**
	 * TODO Have an interface for generating the WHERE clause so that it can support different SQL dialects
	 *
	 * @param items
	 * @throws Exception
	 */
	@Override
	public void write(List<? extends Map<String, Object>> items) {
		StringBuilder whereClause = new StringBuilder("(");
		for (int i = 0; i < items.size(); i++) {
			if (i > 0) {
				whereClause.append(" OR ");
			}
			Map<String, Object> item = items.get(i);
			Object keyValue = item.get(primaryKeyColumnName);
			if (keyValue == null) {
				throw new RuntimeException("Unable to find key value in item: " + item);
			}
			whereClause.append(primaryKeyColumnName).append("='").append(keyValue).append("'");
		}
		whereClause.append(")");
	}
}
