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
	private String rootLocalName = "CHANGEME";

	// Expected to be role,capability,role,capability,etc.
	private String[] permissions;

	private String[] collections;

	public ColumnMapProcessor(ColumnMapSerializer columnMapSerializer) {
		this.columnMapSerializer = columnMapSerializer;
	}

	@Override
	public MarkLogicWriteHandle process(Map<String, Object> item) throws Exception {
		String content = columnMapSerializer.serializeColumnMap(item, rootLocalName);

		// TODO Use UriGenerator
		String uuid = UUID.randomUUID().toString();
		String uri = "/" + rootLocalName + "/" + uuid + ".xml";

		DocumentMetadataHandle metadata = new DocumentMetadataHandle();
		if (collections != null) {
			metadata.withCollections(collections);
		}

		if (permissions != null) {
			for (int i = 0; i < permissions.length; i += 2) {
				String role = permissions[i];
				DocumentMetadataHandle.Capability c = DocumentMetadataHandle.Capability.valueOf(permissions[i + 1].toUpperCase());
				metadata.withPermission(role, c);
			}
		}

		return new MarkLogicWriteHandle(uri, metadata, new StringHandle(content));
	}

	public void setRootLocalName(String rootLocalName) {
		this.rootLocalName = rootLocalName;
	}

	public void setCollections(String[] collections) {
		this.collections = collections;
	}

	public void setPermissions(String[] permissions) {
		this.permissions = permissions;
	}
}
