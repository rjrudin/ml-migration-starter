package org.example;

import com.marklogic.spring.batch.core.launch.support.CommandLineJobRunner;
import joptsimple.OptionParser;

import java.util.Arrays;

/**
 * Entry point for application; extends marklogic-spring-batch's CommandLineJobRunner and defines the command-line
 * arguments that are supported.
 */
public class Main extends CommandLineJobRunner {

	public static void main(String[] args) throws Exception {
		String[] constantArgs = {
			"--job_path", MigrationConfig.class.getName(),
			"--job_id", "job"
		};

		String[] finalArgs = new String[constantArgs.length + args.length];
		System.arraycopy(constantArgs, 0, finalArgs, 0, constantArgs.length);
		System.arraycopy(args, 0, finalArgs, 4, args.length);

		new Main().execute(finalArgs);
	}

	@Override
	protected OptionParser buildOptionParser() {
		OptionParser parser = super.buildOptionParser();
		parser.acceptsAll(Arrays.asList("h", HELP), "Show help").forHelp();
		parser.accepts("all_tables", "Set this to 'true' to ignore the 'sql' argument and read rows from all tables").withRequiredArg();
		parser.accepts("collections", "Comma-delimited sequence of collections to insert each document into").withRequiredArg();
		parser.accepts("document_type", "Valid values: XML, JSON").withRequiredArg();
		parser.accepts("output_uri_prefix", "Output prefix for the URI of each document written to MarkLogic").withRequiredArg();
		parser.accepts("permissions", "Comma-delimited sequence of permissions to apply to each document; e.g. role,capability,role,capability,etc").withRequiredArg();
		parser.accepts("root_local_name", "Name of the root element in each document written to MarkLogic").withRequiredArg();
		parser.accepts("sql", "The SQL query for selecting rows to migrate").withRequiredArg();
		parser.accepts("thread_count", "The number of threads to use for writing to MarkLogic").withRequiredArg();

		parser.accepts("host", "Host name of the MarkLogic server to connect to").withRequiredArg();
		parser.accepts("port", "Port of the MarkLogic REST server to connect to").withRequiredArg();
		parser.accepts("username", "Username to use when connecting to MarkLogic").withRequiredArg();
		parser.accepts("password", "Password for the MarkLogic username").withRequiredArg();

		parser.accepts("jdbc_url", "JDBC URL for connecting to an RDBMS").withRequiredArg();
		parser.accepts("jdbc_driver", "JDBC driver class name for connecting to an RDBMS").withRequiredArg();
		parser.accepts("jdbc_username", "Username for connecting to an RDBMS").withRequiredArg();
		parser.accepts("jdbc_password", "Password for the RDBMS username").withRequiredArg();

		return parser;
	}
}
