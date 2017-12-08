package com.marklogic.batch.rdbms;

import com.marklogic.spring.batch.item.processor.support.UriGenerator;

import java.util.Map;
import java.util.UUID;

public class ColumnMapUriGenerator implements UriGenerator<Map<String, Object>> {

    private String columnId;

    public ColumnMapUriGenerator(String columnId) {
        this.columnId = columnId;
    }

    @Override
    public String generateUri(Map<String, Object> stringObjectMap) throws Exception {
        return stringObjectMap.containsKey(columnId) ?
                stringObjectMap.get(columnId).toString() : UUID.randomUUID().toString();    }
}
