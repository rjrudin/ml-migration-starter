package com.marklogic.migration.rdbms;

import java.util.ArrayList;
import java.util.List;

public class TableQuery {

	private String query;
	// TODO Support compound keys
	private String primaryKeyColumnName;
	private String elementName;

	// Optional - for child table
	private String foreignKeyColumnName;

	private List<TableQuery> childQueries = new ArrayList<>();

	public TableQuery(String query, String primaryKeyColumnName, String foreignKeyColumnName, String elementName) {
		this.query = query;
		this.primaryKeyColumnName = primaryKeyColumnName;
		this.foreignKeyColumnName = foreignKeyColumnName;
		this.elementName = elementName;
	}

	public void addChildQuery(TableQuery tableQuery) {
		this.childQueries.add(tableQuery);
	}

	public String getQuery() {
		return query;
	}

	public String getPrimaryKeyColumnName() {
		return primaryKeyColumnName;
	}

	public String getElementName() {
		return elementName;
	}

	public String getForeignKeyColumnName() {
		return foreignKeyColumnName;
	}

	public List<TableQuery> getChildQueries() {
		return childQueries;
	}
}
