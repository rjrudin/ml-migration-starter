package com.marklogic.spring.batch.columnmap;

import java.util.Map;

/**
 * Defines how a column map should be serialized to XML. When the column map has been processed via
 * PathAwareColumnMapProcessor, then the value of a key/value pair in the map may be a Map<String,Object> itself, or a
 * List of such maps, or simply a List of strings.
 *
 * COPY/PASTED from rowToDoc.
 */
public interface ColumnMapSerializer {

    String serializeColumnMap(Map<String, Object> columnMap, String rootLocalName, String rootNamespaceUri);
}
