package com.marklogic.client.batch;

import com.marklogic.client.helper.LoggingObject;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ExecutorConfigurationSupport;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public abstract class BatchWriterSupport extends LoggingObject implements BatchWriter {

	private TaskExecutor taskExecutor;
	private int threadCount = 16;

	@Override
	public void initialize() {
		if (taskExecutor == null) {
			initializeDefaultTaskExecutor();
		}
	}

	@Override
	public void waitForCompletion() {
		if (taskExecutor instanceof ExecutorConfigurationSupport) {
			if (logger.isInfoEnabled()) {
				logger.info("Calling shutdown on thread pool");
			}
			((ExecutorConfigurationSupport) taskExecutor).shutdown();
			if (logger.isInfoEnabled()) {
				logger.info("Thread pool finished shutdown");
			}
		}
	}

	protected void initializeDefaultTaskExecutor() {
		if (threadCount > 1) {
			if (logger.isInfoEnabled()) {
				logger.info("Initializing thread pool with a count of " + threadCount);
			}
			ThreadPoolTaskExecutor tpte = new ThreadPoolTaskExecutor();
			tpte.setCorePoolSize(threadCount);
			// By default, wait for tasks to finish, and wait up to an hour
			tpte.setWaitForTasksToCompleteOnShutdown(true);
			tpte.setAwaitTerminationSeconds(60 * 60);
			tpte.afterPropertiesSet();
			this.taskExecutor = tpte;
		} else {
			if (logger.isInfoEnabled()) {
				logger.info("Thread count is 1, so using a synchronous TaskExecutor");
			}
			this.taskExecutor = new SyncTaskExecutor();
		}
	}

	protected void execute(Runnable runnable) {
		if (taskExecutor instanceof AsyncTaskExecutor && false) {
			((AsyncTaskExecutor) taskExecutor).submit(runnable);
		} else {
			taskExecutor.execute(runnable);
		}
	}

	public void setTaskExecutor(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}
}
