package com.marklogic.client.xcc;

import com.marklogic.client.document.DocumentWriteOperation;
import com.marklogic.client.io.*;
import com.marklogic.client.io.marker.AbstractWriteHandle;
import com.marklogic.client.io.marker.DocumentMetadataWriteHandle;
import com.marklogic.xcc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DefaultDocumentWriteOperationAdapter implements DocumentWriteOperationAdapter {

	private final static Logger logger = LoggerFactory.getLogger(DefaultDocumentWriteOperationAdapter.class);

	@Override
	public Content adapt(DocumentWriteOperation operation) {
		String uri = operation.getUri();
		ContentCreateOptions options = adaptMetadata(operation.getMetadata());
		AbstractWriteHandle handle = operation.getContent();
		if (handle instanceof StringHandle) {
			return ContentFactory.newContent(uri, ((StringHandle) handle).get(), options);
		} else if (handle instanceof FileHandle) {
			return ContentFactory.newContent(uri, ((FileHandle) handle).get(), options);
		} else if (handle instanceof BytesHandle) {
			return ContentFactory.newContent(uri, ((BytesHandle) handle).get(), options);
		} else if (handle instanceof InputStreamHandle) {
			try {
				return ContentFactory.newContent(uri, ((InputStreamHandle) handle).get(), options);
			} catch (IOException e) {
				throw new RuntimeException("Unable to read content input stream: " + e.getMessage(), e);
			}
		} else if (handle instanceof DOMHandle) {
			return ContentFactory.newContent(uri, ((DOMHandle) handle).get(), options);
		} else throw new IllegalArgumentException("No support yet for content class: " + handle.getClass().getName());
	}

	/**
	 * TODO Only adapts collections, quality, and permissions so far. Should add Format support too.
	 *
	 * @param handle
	 * @return
	 */
	protected ContentCreateOptions adaptMetadata(DocumentMetadataWriteHandle handle) {
		ContentCreateOptions options = new ContentCreateOptions();
		if (handle instanceof DocumentMetadataHandle) {
			DocumentMetadataHandle metadata = (DocumentMetadataHandle) handle;
			options.setQuality(metadata.getQuality());

			options.setCollections(metadata.getCollections().toArray(new String[]{}));

			Set<ContentPermission> contentPermissions = new HashSet<>();
			DocumentMetadataHandle.DocumentPermissions permissions = metadata.getPermissions();
			for (String role : permissions.keySet()) {
				for (DocumentMetadataHandle.Capability capability : permissions.get(role)) {
					ContentCapability contentCapability;
					if (DocumentMetadataHandle.Capability.EXECUTE.equals(capability)) {
						contentCapability = ContentCapability.EXECUTE;
					}
					else if (DocumentMetadataHandle.Capability.INSERT.equals(capability)) {
						contentCapability = ContentCapability.INSERT;
					}
					else if (DocumentMetadataHandle.Capability.READ.equals(capability)) {
						contentCapability = ContentCapability.READ;
					}
					else if (DocumentMetadataHandle.Capability.UPDATE.equals(capability)) {
						contentCapability = ContentCapability.UPDATE;
					}
					else throw new IllegalArgumentException("Unrecognized permission capability: " + capability);
					contentPermissions.add(new ContentPermission(contentCapability, role));
				}
			}
			options.setPermissions(contentPermissions.toArray(new ContentPermission[]{}));

		} else {
			logger.warn("Only supports DocumentMetadataHandle; unsupported metadata class: " + handle.getClass().getName());
		}
		return options;
	}
}
