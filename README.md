# Making database migrations to MarkLogic easy

This is a starter kit for creating an application that uses [Spring Batch](http://projects.spring.io/spring-batch/) and
[marklogic-spring-batch](https://github.com/sastafford/marklogic-spring-batch) for migrating data from any database with a JDBC driver into 
MarkLogic. The intent is to simplify the process of creating a migration application using Spring Batch by 
leveraging the reusable components in marklogic-spring-batch, and by organizing a Gradle-based project for you that you
can clone/fork/etc to quickly extend and customize for your specific needs. 

This project provides the following features:

1. Provides a simple way to get started with Spring Batch - just configure the JDBC/MarkLogic connection properties, provide your
database's JDBC driver, write a SQL query, and you're off and running
1. Given a SQL query, or for every table in the database, uses a Spring JDBC [ColumnMapRowMapper](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/jdbc/core/ColumnMapRowMapper.html) 
to read every row as a map
1. Uses [StAX](https://docs.oracle.com/javase/tutorial/jaxp/stax/api.html) to convert that map into an XML document
1. Uses [BatchWriter in ml-javaclient-util](https://github.com/rjrudin/ml-javaclient-util#parallelized-batch-writes) to write batches
of documents across many hosts in a cluster and via a configurable [Spring thread pool](https://docs.spring.io/spring/docs/current/spring-framework-reference/html/scheduling.html)
1. Use either the [MarkLogic Java Client](https://docs.marklogic.com/guide/java) or [XCC](https://docs.marklogic.com/guide/xcc/intro) to load data
(the new Data Movement SDK will be supported once MarkLogic 9 is available)

This project has the following defaults in place that you can use as a starting point:

1. Sets up and reads data from an [H2](http://www.h2database.com/html/main.html) test database - this is only used for a quick demonstration and can be
easily removed from the project.
1. Defaults to writing to MarkLogic using localhost/8000/admin/admin
1. Has a Gradle task for launching the migration - "./gradlew migrate"

The fact that this uses H2 by default is only so you can kick the tires on it. Instructions are below for how to change
the JDBC configuration properties to point to your own database. 

## How do I try this out?

To try this out locally with the default configuration for H2, just do the following:

1. Clone this repo
1. Verify you have ML 8+ installed locally and that port 8000 (the default one) points to the Documents database 
(you can of course modify this to write to any database you want)
1. Verify that the username/password properties in gradle.properties are correct for your MarkLogic cluster (it's best 
not to use the admin user unless absolutely necessary, but this defaults to it for the sake of convenience)
1. Run ./gradlew setupH2 migrate

You should see some logging like this:

    22:57:03.628 [main] INFO  org.example.MigrationConfig - Chunk size: 100
    22:57:03.630 [main] INFO  org.example.MigrationConfig - Hosts: localhost
    22:57:03.631 [main] INFO  org.example.MigrationConfig - Migrate all tables: true
    22:57:03.631 [main] INFO  org.example.MigrationConfig - Permissions: rest-reader,read,rest-writer,update
    22:57:03.631 [main] INFO  org.example.MigrationConfig - Thread count: 16
    22:57:03.640 [main] INFO  org.example.MigrationConfig - Client username: admin
    22:57:03.640 [main] INFO  org.example.MigrationConfig - Client database: Documents
    22:57:03.640 [main] INFO  org.example.MigrationConfig - Client authentication: DIGEST
    22:57:03.640 [main] INFO  org.example.MigrationConfig - Creating client for host: localhost
    22:57:03.840 [main] INFO  org.example.MigrationConfig - Initialized components, launching job
    22:57:04.142 [main] INFO  c.m.s.b.i.writer.MarkLogicItemWriter - On stream open, initializing BatchWriter
    22:57:04.143 [main] INFO  c.m.client.batch.RestBatchWriter - Initializing thread pool with a count of 16
    22:57:04.148 [main] INFO  c.m.s.b.i.writer.MarkLogicItemWriter - On stream open, finished initializing BatchWriter
    22:57:04.205 [main] INFO  c.m.s.b.i.reader.AllTablesItemReader - Finished reading rows for query: SELECT * FROM CUSTOMER
    22:57:04.400 [main] INFO  c.m.s.b.i.reader.AllTablesItemReader - Finished reading rows for query: SELECT * FROM INVOICE
    22:57:04.469 [main] INFO  c.m.s.b.i.reader.AllTablesItemReader - Finished reading rows for query: SELECT * FROM ITEM
    22:57:04.477 [main] INFO  c.m.s.b.i.reader.AllTablesItemReader - Finished reading rows for query: SELECT * FROM PRODUCT
    22:57:04.489 [main] INFO  c.m.s.b.i.writer.MarkLogicItemWriter - On stream close, waiting for BatchWriter to complete
    22:57:04.489 [main] INFO  c.m.client.batch.RestBatchWriter - Calling shutdown on thread pool
    22:57:05.320 [ThreadPoolTaskExecutor-8] INFO  c.m.client.batch.RestBatchWriter - Wrote 100 documents to MarkLogic
    22:57:05.322 [ThreadPoolTaskExecutor-4] INFO  c.m.client.batch.RestBatchWriter - Wrote 100 documents to MarkLogic
    22:57:05.324 [ThreadPoolTaskExecutor-1] INFO  c.m.client.batch.RestBatchWriter - Wrote 100 documents to MarkLogic
    22:57:05.326 [ThreadPoolTaskExecutor-2] INFO  c.m.client.batch.RestBatchWriter - Wrote 100 documents to MarkLogic
    22:57:05.326 [ThreadPoolTaskExecutor-7] INFO  c.m.client.batch.RestBatchWriter - Wrote 100 documents to MarkLogic
    22:57:05.327 [ThreadPoolTaskExecutor-3] INFO  c.m.client.batch.RestBatchWriter - Wrote 100 documents to MarkLogic
    22:57:05.330 [ThreadPoolTaskExecutor-6] INFO  c.m.client.batch.RestBatchWriter - Wrote 100 documents to MarkLogic
    22:57:05.333 [ThreadPoolTaskExecutor-5] INFO  c.m.client.batch.RestBatchWriter - Wrote 100 documents to MarkLogic
    22:57:05.334 [main] INFO  c.m.client.batch.RestBatchWriter - Thread pool finished shutdown
    22:57:05.334 [main] INFO  c.m.client.batch.RestBatchWriter - Releasing DatabaseClient instances...
    22:57:05.334 [main] INFO  c.m.client.batch.RestBatchWriter - Finished releasing DatabaseClient instances
    22:57:05.334 [main] INFO  c.m.s.b.i.writer.MarkLogicItemWriter - On stream close, finished waiting for BatchWriter to complete
    22:57:05.334 [main] INFO  c.m.s.b.i.writer.MarkLogicItemWriter - Final Write Count: 800

When using the sample H2 database, you only need to run "setupH2" once. And of course, when you're using your own 
database, you can remove this task from build.gradle.

The migration configuration properties are all in gradle.properties. You can modify those properties on the command line
via Gradle's -P mechanism, e.g.

    ./gradlew migrate -Phosts=host1,host2,host3 -Pthread_count=32

Or load the data via XCC instead of the REST API:

    ./gradlew migrate -Pxcc=true

Or load the data as JSON instead of XML:

    ./gradlew migrate -Pdocument_type=json

Or just modify gradle.properties and start building your own application. 

You can also see all the supported arguments:

    ./gradlew help

Comments,questions - please file an issue.

## How do I make this work with my own database?

To try this on your own database, you'll need to change the JDBC connection properties in gradle.properties - jdbc_url,
jdbc_username, and jdbc_password. 

Assuming you're not using H2, you'll also need to change the jdbc_driver property in gradle.properties. You'll need to add
your JDBC driver to the Gradle "runtime" classpath. The H2 driver is currently in that classpath via the following 
line in build.gradle:

    runtime "com.h2database:h2:1.4.193"

You can either change that line to reference your own JDBC driver in a Maven repository, or
remove it and replace it with a line that specifies where your driver is on the classpath, e.g.:

    runtime files("./path/to/ojdbc-6.jar")

Next, by default, the migration program tries to migrate all tables that it finds in the database. You can either retain
this behavior by keeping the "all_tables" property set to "true", or you can set that property to "false" and specify a
SQL query, a local name for the root element of each XML document that's written, and the collections to put each 
document into - example:

    sql=SELECT * FROM Customer
    root_local_name=customer
    collections=customer,migrated

If you do keep "all_tables" set to "true", it's best to keep "collections" set to something generic that each document
will be written to - example:

    all_tables=true
    collections=migrated

At this point, you're able to reconfigure the migration job to talk to any database, run any SQL query, and set any
local name for the root element, any collection, and any permission. 

### But how do I modify the XML that's inserted into MarkLogic?

The way the batch job works is defined by the org.example.MigrationConfig class. This class creates a Spring Batch
Reader, Writer, and Processor (for more information on these concepts, definitely check out the 
[Spring Batch user manual](http://docs.spring.io/spring-batch/reference/html/)). 

The XML is currently generated by the org.example.ColumnMapProcessor class. This is a quick-and-dirty Spring Batch
Processor implementation that uses a simple [StAX](https://docs.oracle.com/javase/tutorial/jaxp/stax/api.html)-based
approach for converting a Spring ColumnMap (a map of column names and values; I'm using this term as shorthand for a 
Map<String, Object>) into an XML document. 

To modify how this works, you'll need to write code, which opens the door to all the batch-processing power and 
flexibility provided by Spring Batch. Here are a few paths to consider:

1. Modify the ColumnMapProcessor with your own method for converting a ColumnMap into a String of XML, or JSON
1. Write your own Processor implementation from scratch and use that, and modify MigrationConfig to use your Processor
1. Write your own Reader that returns something besides a ColumnMap. Modify MigrationConfig to use this new Reader, 
and you'll need to modify the Processor as well, which expects a ColumnMap.
1. You can even replace the Writer, which depends on a MarkLogic Java Client DocumentWriteOperation instance. Typically
though, you'll be able to retain this part by having your Reader and/or Processor return a DocumenteWriteOperation, 
which encapsulates all the information needed to write a single document to MarkLogic.

### How do I setup automated tests for my migration program?

TODO Coming soon!
