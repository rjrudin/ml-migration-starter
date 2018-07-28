package org.example;

public class ChildQuery {

	private String elementName;
	private String query;

	public ChildQuery(String elementName, String query) {
		this.elementName = elementName;
		this.query = query;
	}

	public String getElementName() {
		return elementName;
	}

	public String getQuery() {
		return query;
	}
}
