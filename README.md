# Making migrations from relational databases to MarkLogic easy

This is a starter kit for creating your own project that uses [Spring Batch](http://projects.spring.io/spring-batch/) and
[marklogic-spring-batch](https://github.com/sastafford/marklogic-spring-batch) for migrating data from any RDBMS into MarkLogic. You 
can clone/fork/do whatever you want with this repository to get your own project going.

This project has the following defaults in place that you can use as a starting point:

1. Talks to a local MySQL database with the [Sakila](https://dev.mysql.com/doc/sakila/en/) database loaded in it, using the MySQL JDBC driver (easily customized to use any database with any JDBC driver)
1. Uses Spring's ColumnMapReader to turn every row into a Map<String, Object>
1. Uses StAX to turn that Map<String, Object> into a simple XML document
1. Uses the ML Java API to write to MarkLogic
1. Defaults to writing to localhost/8000/admin/admin
1. Supports writing to any number of hosts in a cluster
1. Supports a configurable thread pool for how many threads you want writing to MarkLogic
1. Has a Gradle task for launching the migration - "./gradlew migrate"

The support for writing to any number of hosts and parallelizing writes via a thread pool is provided by the 
[BatchWriter library in ml-javaclient-util](https://github.com/rjrudin/ml-javaclient-util/tree/dev/src/main/java/com/marklogic/client/batch). 
Once MarkLogic 9 is available, these features will instead be provided via [DMSDK](https://github.com/marklogic/data-movement).

To try this out locally, just do the following:

1. Clone this repo
1. Install and start MySQL
1. [Load the Sakila dataset](https://dev.mysql.com/doc/sakila/en/sakila-installation.html)
1. Verify you have ML 8+ installed locally and that port 8000 (the default one) points to the Documents database (you can of course modify this to write to any database you want)
1. Run ./gradlew migrate

You should see some nice logging like this:

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


The default configuration is all in gradle.properties. You can modify those on the command line, e.g.

    ./gradlew migrate -Phosts=host1,host2,host3 -PthreadCount=32
    
Or just modify the file and start building your own migration. 

You can also see all the supported arguments:

    ./gradlew help

Comments,questions - please file an issue.
