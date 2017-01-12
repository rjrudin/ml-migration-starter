package com.marklogic.spring.batch.item.writer;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.document.DocumentWriteOperation;
import com.marklogic.client.document.DocumentWriteSet;
import com.marklogic.client.document.GenericDocumentManager;
import com.marklogic.client.helper.LoggingObject;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemWriter;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * This is meant for ML clusters prior to ML9, which can't take advantage of the great features in DMSDK for
 * parallelizing writes to an ML cluster. It will round robin requests across a list of DatabaseClient's, and it'll
 * delegate all the work to a thread pool of a configurable size.
 */
public class ParallelizedMarkLogicItemWriter extends LoggingObject implements ItemWriter<DocumentWriteOperation>, ItemStream {

	private List<DatabaseClient> databaseClients;
	private int clientIndex = 0;

	private TaskExecutor taskExecutor;
	private List<Future<?>> futures = new ArrayList<>();
	private int threadCount = 16;

	public ParallelizedMarkLogicItemWriter(List<DatabaseClient> databaseClients) {
		this.databaseClients = databaseClients;
	}

	@Override
	public void write(List<? extends DocumentWriteOperation> items) throws Exception {
		DatabaseClient client = databaseClients.get(clientIndex);
		clientIndex++;
		if (clientIndex >= databaseClients.size()) {
			clientIndex = 0;
		}
		if (logger.isInfoEnabled()) {
			logger.info("Writing " + items.size() + " documents to MarkLogic");
		}

		Runnable r = new BatchWriter(client, items);
		if (taskExecutor instanceof AsyncTaskExecutor) {
			futures.add(((AsyncTaskExecutor) taskExecutor).submit(r));
		} else {
			taskExecutor.execute(r);
		}
	}

	@Override
	public void open(ExecutionContext executionContext) throws ItemStreamException {
		if (threadCount > 1) {
			if (logger.isInfoEnabled()) {
				logger.info("Initializing thread pool with a count of " + threadCount);
			}
			ThreadPoolTaskExecutor tpte = new ThreadPoolTaskExecutor();
			tpte.setCorePoolSize(threadCount);
			tpte.afterPropertiesSet();
			this.taskExecutor = tpte;
		} else {
			if (logger.isInfoEnabled()) {
				logger.info("Thread count is 1, so using a synchronous TaskExecutor");
			}
			this.taskExecutor = new SyncTaskExecutor();
		}
	}

	@Override
	public void close() throws ItemStreamException {
		logger.info("Waiting for threads to finish document processing; futures count: " + futures.size());
		for (Future<?> f : futures) {
			try {
				f.get(10, TimeUnit.HOURS);
			} catch (Exception ex) {
				logger.warn("Unable to wait for last set of documents to be processed: " + ex.getMessage(), ex);
			}
		}
	}

	@Override
	public void update(ExecutionContext executionContext) throws ItemStreamException {

	}

	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}
}

class BatchWriter implements Runnable {

	private DatabaseClient client;
	private List<? extends DocumentWriteOperation> items;

	public BatchWriter(DatabaseClient client, List<? extends DocumentWriteOperation> items) {
		this.client = client;
		this.items = items;
	}

	@Override
	public void run() {
		GenericDocumentManager mgr = client.newDocumentManager();
		DocumentWriteSet set = mgr.newWriteSet();
		for (DocumentWriteOperation item : items) {
			set.add(item);
		}
		mgr.write(set);
	}
}