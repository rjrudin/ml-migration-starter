package org.example;

import com.marklogic.spring.batch.item.processor.support.UriGenerator;

import java.util.Map;
import java.util.UUID;

/**
 * Implementation of marklogic-spring-batch interface for how a URI should be generated for a particular record that
 * is read from an ItemReader. This example shows how a columnId can be set and use as the source of a URI. If the
 * columnId is not found, then a random UUID is generated.
 */
public class ColumnMapUriGenerator implements UriGenerator<Map<String, Object>> {

	private String columnId;

	public ColumnMapUriGenerator(String columnId) {
		this.columnId = columnId;
	}

	@Override
	public String generateUri(Map<String, Object> stringObjectMap) {
		return stringObjectMap.containsKey(columnId) ?
			stringObjectMap.get(columnId).toString() : UUID.randomUUID().toString();
	}
}
