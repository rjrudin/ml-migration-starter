# Making migrations from relational databases to MarkLogic easy

This is a starter kit for creating an application that uses [Spring Batch](http://projects.spring.io/spring-batch/) and
[marklogic-spring-batch](https://github.com/sastafford/marklogic-spring-batch) for migrating data from any RDBMS into MarkLogic. You 
can clone/fork/do whatever you want with this repository to get your own application going.

This project provides the following features:

1. Provides a simple way to get started with Spring Batch - just configure the JDBC/MarkLogic connection properties, provide your
database's JDBC driver, write a SQL query, and you're off and running
1. Given a SQL query, uses a Spring JDBC [ColumnMapRowMapper](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/jdbc/core/ColumnMapRowMapper.html) 
to read every row as a map
1. Uses [StAX](https://docs.oracle.com/javase/tutorial/jaxp/stax/api.html) to convert that map into an XML document
1. Uses [BatchWriter in ml-javaclient-util](https://github.com/rjrudin/ml-javaclient-util#parallelized-batch-writes) to write batches
of documents across many hosts in a cluster and via a configurable [Spring thread pool](https://docs.spring.io/spring/docs/current/spring-framework-reference/html/scheduling.html)
1. Use either the [MarkLogic Java Client](https://docs.marklogic.com/guide/java) or [XCC](https://docs.marklogic.com/guide/xcc/intro) to load data
(the new Data Movement SDK will be supported once MarkLogic 9 is available)

This project has the following defaults in place that you can use as a starting point:

1. Talks to a local MySQL database with the [Sakila](https://dev.mysql.com/doc/sakila/en/) database loaded in it, using 
the MySQL JDBC driver (this is easily customized to use any database with any JDBC driver)
1. Defaults to writing to MarkLogic using localhost/8000/admin/admin
1. Has a Gradle task for launching the migration - "./gradlew migrate"

## How do I try this out?

To try this out locally with the default configuration for MySQL, just do the following:

1. Clone this repo
1. Install and start MySQL
1. [Load the Sakila dataset](https://dev.mysql.com/doc/sakila/en/sakila-installation.html)
1. Verify you have ML 8+ installed locally and that port 8000 (the default one) points to the Documents database 
(you can of course modify this to write to any database you want)
1. Verify that the username/password properties in gradle.properties are correct for your MarkLogic cluster (it's best 
not to use the admin user unless absolutely necessary, but this defaults to it for the sake of convenience)
1. Run ./gradlew migrate

You should see some logging like this:

    14:33:43.053 [main] INFO  org.example.MigrationConfig - Chunk size: 100
    14:33:43.055 [main] INFO  org.example.MigrationConfig - Hosts: localhost
    14:33:43.055 [main] INFO  org.example.MigrationConfig - SQL: SELECT * FROM film
    14:33:43.055 [main] INFO  org.example.MigrationConfig - Root local name: Film
    14:33:43.056 [main] INFO  org.example.MigrationConfig - Collections: film,migrated
    14:33:43.056 [main] INFO  org.example.MigrationConfig - Permissions: rest-reader,read,rest-writer,update
    14:33:43.056 [main] INFO  org.example.MigrationConfig - Thread count: 16
    14:33:43.068 [main] INFO  org.example.MigrationConfig - Client username: admin
    14:33:43.068 [main] INFO  org.example.MigrationConfig - Client database: Documents
    14:33:43.068 [main] INFO  org.example.MigrationConfig - Client authentication: DIGEST
    14:33:43.068 [main] INFO  org.example.MigrationConfig - Creating client for host: localhost
    14:33:43.287 [main] INFO  org.example.MigrationConfig - Initialized components, launching job
    14:33:43.580 [main] INFO  c.m.s.b.item.writer.BatchItemWriter - On stream open, initializing BatchWriter
    14:33:43.581 [main] INFO  c.m.client.batch.RestBatchWriter - Initializing thread pool with a count of 16
    14:33:43.584 [main] INFO  c.m.s.b.item.writer.BatchItemWriter - On stream open, finished initializing BatchWriter
    14:33:44.099 [main] INFO  c.m.s.b.item.writer.BatchItemWriter - On stream close, waiting for BatchWriter to complete
    14:33:44.099 [main] INFO  c.m.client.batch.RestBatchWriter - Calling shutdown on thread pool
    14:33:44.961 [ThreadPoolTaskExecutor-10] INFO  c.m.client.batch.RestBatchWriter - Wrote 100 documents to MarkLogic
    14:33:44.962 [ThreadPoolTaskExecutor-4] INFO  c.m.client.batch.RestBatchWriter - Wrote 100 documents to MarkLogic
    14:33:44.965 [ThreadPoolTaskExecutor-9] INFO  c.m.client.batch.RestBatchWriter - Wrote 100 documents to MarkLogic
    14:33:44.968 [ThreadPoolTaskExecutor-6] INFO  c.m.client.batch.RestBatchWriter - Wrote 100 documents to MarkLogic
    14:33:44.973 [ThreadPoolTaskExecutor-5] INFO  c.m.client.batch.RestBatchWriter - Wrote 100 documents to MarkLogic
    14:33:44.979 [ThreadPoolTaskExecutor-3] INFO  c.m.client.batch.RestBatchWriter - Wrote 100 documents to MarkLogic
    14:33:44.979 [ThreadPoolTaskExecutor-1] INFO  c.m.client.batch.RestBatchWriter - Wrote 100 documents to MarkLogic
    14:33:44.980 [ThreadPoolTaskExecutor-8] INFO  c.m.client.batch.RestBatchWriter - Wrote 100 documents to MarkLogic
    14:33:44.980 [ThreadPoolTaskExecutor-7] INFO  c.m.client.batch.RestBatchWriter - Wrote 100 documents to MarkLogic
    14:33:44.985 [ThreadPoolTaskExecutor-2] INFO  c.m.client.batch.RestBatchWriter - Wrote 100 documents to MarkLogic
    14:33:44.985 [main] INFO  c.m.client.batch.RestBatchWriter - Thread pool finished shutdown
    14:33:44.985 [main] INFO  c.m.client.batch.RestBatchWriter - Releasing DatabaseClient instances...
    14:33:44.985 [main] INFO  c.m.client.batch.RestBatchWriter - Finished releasing DatabaseClient instances
    14:33:44.985 [main] INFO  c.m.s.b.item.writer.BatchItemWriter - On stream close, finished waiting for BatchWriter to complete


The default configuration is all in gradle.properties. You can modify those properties on the command line, e.g.

    ./gradlew migrate -Phosts=host1,host2,host3 -PthreadCount=32

Or load the data via XCC instead of the REST API:

    ./gradlew migrate -Pxcc=true

Or just modify the file and start building your own application. 

You can also see all the supported arguments:

    ./gradlew help

Comments,questions - please file an issue.

## How do I make this work with my own database?

To try this on your own database, you'll need to change the JDBC connection properties in gradle.properties - jdbcUrl,
jdbcUsername, and jdbcPassword. 

If you're not using MySQL, you'll need to change the jdbcDriver property in gradle.properties. You'll also need to add
your JDBC driver to the Gradle "runtime" classpath. The MySQL driver is currently in that classpath via the following 
line in build.gradle:

    runtime "mysql:mysql-connector-java:5.1.6"

If you're not using MySQL, you can either change that line to reference your own JDBC driver in a Maven repository, or
remove it and replace it with a line that specifies where your driver is on the classpath, e.g.:

    runtime files("./path/to/ojdbc-6.jar")

Next, you'll most likely need to change the SQL query and the basics of how documents are created in MarkLogic. You can 
do that by modifying the following properties in gradle.properties:
    
    sql=SELECT * FROM film
    rootLocalName=Film
    collections=film,migrated
    permissions=rest-reader,read,rest-writer,update
    
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

