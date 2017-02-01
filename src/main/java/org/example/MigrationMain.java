package org.example;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Subclasses the Main program from marklogic-spring-batch so that we can avoid using the default MainConfig class
 * in marklogic-spring-batch.
 */
public class MigrationMain extends com.marklogic.spring.batch.Main {

	public static void main(String[] args) throws Exception {
		new MigrationMain().runJob(args);
	}

	/**
	 * We don't need MainConfig registered, as we're creating our own DatabaseClient's and we don't yet have a need for
	 * the ML JobRepository.
	 *
	 * @param ctx
	 */
	@Override
	protected void registerDefaultConfigurations(AnnotationConfigApplicationContext ctx) {
	}
}
