package com.marklogic.spring.batch.item.processor;

import com.marklogic.client.document.DocumentWriteOperation;
import com.marklogic.client.io.DocumentMetadataHandle;
import com.marklogic.client.io.MarkLogicWriteHandle;
import com.marklogic.client.io.StringHandle;
import com.marklogic.spring.batch.columnmap.ColumnMapSerializer;
import org.springframework.batch.item.ItemProcessor;

import java.util.Map;
import java.util.UUID;

public class ColumnMapProcessor implements ItemProcessor<Map<String, Object>, DocumentWriteOperation> {

	private ColumnMapSerializer columnMapSerializer;

	public ColumnMapProcessor(ColumnMapSerializer columnMapSerializer) {
		this.columnMapSerializer = columnMapSerializer;
	}

	@Override
	public MarkLogicWriteHandle process(Map<String, Object> item) throws Exception {
		String content = columnMapSerializer.serializeColumnMap(item, "CHANGEME", "");
		String uuid = UUID.randomUUID().toString();
		return new MarkLogicWriteHandle(
			uuid + ".xml",
			new DocumentMetadataHandle().withCollections("CHANGEME"),
			new StringHandle(content)
		);
	}
}
