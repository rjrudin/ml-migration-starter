package com.marklogic.spring.batch.item.writer;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.document.DocumentWriteOperation;
import com.marklogic.client.document.DocumentWriteSet;
import com.marklogic.client.document.GenericDocumentManager;
import com.marklogic.client.helper.LoggingObject;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

public class MultipleClientItemWriter extends LoggingObject implements ItemWriter<DocumentWriteOperation> {

	private List<DatabaseClient> databaseClients;
	private int clientIndex = 0;

	public MultipleClientItemWriter(List<DatabaseClient> databaseClients) {
		this.databaseClients = databaseClients;
	}

	@Override
	public void write(List<? extends DocumentWriteOperation> items) throws Exception {
		DatabaseClient client = databaseClients.get(clientIndex);
		clientIndex++;
		if (clientIndex >= databaseClients.size()) {
			clientIndex = 0;
		}

		GenericDocumentManager mgr = client.newDocumentManager();
		DocumentWriteSet set = mgr.newWriteSet();
		for (DocumentWriteOperation item : items) {
			set.add(item);
		}
		if (logger.isInfoEnabled()) {
			logger.info("Writing " + items.size() + " documents to MarkLogic");
		}
		mgr.write(set);
	}
}
