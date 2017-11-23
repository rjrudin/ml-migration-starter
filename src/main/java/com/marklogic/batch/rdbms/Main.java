package com.marklogic.batch.rdbms;

import com.marklogic.spring.batch.core.launch.support.CommandLineJobRunner;
import joptsimple.OptionParser;

import java.util.Arrays;

public class Main extends CommandLineJobRunner {

    public static void main(String[] args) throws Exception {
        String[] constantArgs = {"--job_path", "com.marklogic.batch.rdbms.MigrateRdbmsToMarkLogicJobConfig",
                "--job_id", "job"};
        String[] finalArgs = new String[constantArgs.length + args.length];
        finalArgs[0] = constantArgs[0];
        finalArgs[1] = constantArgs[1];
        finalArgs[2] = constantArgs[2];
        finalArgs[3] = constantArgs[3];

        for (int i = 0; i < args.length; i++) {
            finalArgs[i + 4] = args[0];
        }

        new Main().execute(finalArgs);
    }

    @Override
    protected OptionParser buildOptionParser() {
        OptionParser parser = super.buildOptionParser();
        parser.acceptsAll(Arrays.asList("h", HELP), "Show help").forHelp();
        parser.accepts("all_tables", "Set this to 'true' to ignore the 'sql' argument and read rows from all tables").withRequiredArg();
        parser.accepts("collections", "Comma-delimited sequence of collections to insert each document into").withRequiredArg().defaultsTo("raw");
        parser.accepts("hosts", "Comma-delimited sequence of host names of MarkLogic nodes to write documents to").withRequiredArg();
        parser.accepts("permissions", "Comma-delimited sequence of permissions to apply to each document; role,capability,role,capability,etc").withRequiredArg();
        parser.accepts("root_local_name", "Name of the root element in each document written to MarkLogic").withRequiredArg().defaultsTo("root");
        parser.accepts("sql", "The SQL query for selecting rows to migrate").withRequiredArg();
        parser.accepts("thread_count", "The number of threads to use for writing to MarkLogic").withRequiredArg().defaultsTo("8");
        parser.accepts("xcc", "Set to 'true' to use XCC instead of the REST API to write to MarkLogic").withRequiredArg();
        return parser;
    }
}
