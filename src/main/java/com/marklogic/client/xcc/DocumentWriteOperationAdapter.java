package com.marklogic.client.xcc;

import com.marklogic.client.document.DocumentWriteOperation;
import com.marklogic.xcc.Content;

public interface DocumentWriteOperationAdapter {

	Content adapt(DocumentWriteOperation operation);

}
