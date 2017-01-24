package com.marklogic.spring.batch.item.writer;

import com.marklogic.client.batch.BatchWriter;
import com.marklogic.client.document.DocumentWriteOperation;
import com.marklogic.client.helper.LoggingObject;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

/**
 * Depends on an instance of BatchWriter from ml-javaclient-util.
 *
 * TODO Migrate this to marklogic-spring-batch.
 */
public class BatchItemWriter extends LoggingObject implements ItemWriter<DocumentWriteOperation>, ItemStream {

	private BatchWriter batchWriter;

	public BatchItemWriter(BatchWriter batchWriter) {
		this.batchWriter = batchWriter;
	}

	@Override
	public void write(List<? extends DocumentWriteOperation> items) throws Exception {
		batchWriter.write(items);
	}

	@Override
	public void open(ExecutionContext executionContext) throws ItemStreamException {
		if (logger.isInfoEnabled()) {
			logger.info("On stream open, initializing BatchWriter");
		}
		batchWriter.initialize();
		if (logger.isInfoEnabled()) {
			logger.info("On stream open, finished initializing BatchWriter");
		}
	}

	@Override
	public void close() throws ItemStreamException {
		if (logger.isInfoEnabled()) {
			logger.info("On stream close, waiting for BatchWriter to complete");
		}
		batchWriter.waitForCompletion();
		if (logger.isInfoEnabled()) {
			logger.info("On stream close, finished waiting for BatchWriter to complete");
		}
	}

	@Override
	public void update(ExecutionContext executionContext) throws ItemStreamException {
	}
}
