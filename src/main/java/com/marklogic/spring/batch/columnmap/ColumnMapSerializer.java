package com.marklogic.spring.batch.columnmap;

import java.util.Map;

/**
 * COPY/PASTED from rowToDoc. Should probably live in marklogic-spring-batch.
 */
public interface ColumnMapSerializer {

    String serializeColumnMap(Map<String, Object> columnMap, String rootLocalName);
}
