package com.marklogic.client.batch;

import com.marklogic.client.document.DocumentWriteOperation;

import java.util.List;

/**
 * TODO Separate this into a separate github project so it's a standalone library for pre-ML9 use cases that need to
 * do multi-threaded batch writes to MarkLogic.
 */
public interface BatchWriter {

	public void initialize();

	public void write(List<? extends DocumentWriteOperation> items);

	public void waitForCompletion();
}
