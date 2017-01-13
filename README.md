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
1. Supports writing to any number of hosts in a cluster (once ML9 is out, we can use [DMSDK](https://github.com/marklogic/data-movement) for this instead)
1. Supports a configurable thread pool for how many threads you want writing to MarkLogic (another feature DMSDK will provide with ML9)
1. Has a Gradle task for launching the migration - "./gradlew migrate"

To try this out locally, just do the following:

1. Clone this repo
1. Install and start MySQL
1. [Load the Sakila dataset](https://dev.mysql.com/doc/sakila/en/sakila-installation.html)
1. Verify you have ML 8+ installed locally and that port 8000 (the default one) points to the Documents database (you can of course modify this to write to any database you want)
1. Run ./gradlew migrate

You should see some nice logging like this:

    18:20:10.057 [main] INFO  org.example.MigrationConfig - Chunk size: 100
    18:20:10.057 [main] INFO  org.example.MigrationConfig - Hosts: localhost
    18:20:10.057 [main] INFO  org.example.MigrationConfig - SQL: SELECT * FROM film
    18:20:10.057 [main] INFO  org.example.MigrationConfig - Root local name: Film
    18:20:10.057 [main] INFO  org.example.MigrationConfig - Collections: film
    18:20:10.057 [main] INFO  org.example.MigrationConfig - Thread count: 16
    18:20:10.073 [main] INFO  org.example.MigrationConfig - Creating client for host: localhost
    18:20:10.525 [main] INFO  c.m.s.b.i.w.ParallelizedMarkLogicItemWriter - Initializing thread pool with a count of 16
    18:20:14.394 [ThreadPoolTaskExecutor-3] INFO  c.m.s.batch.item.writer.BatchWriter - Wrote 100 documents to MarkLogic
    18:20:14.394 [ThreadPoolTaskExecutor-2] INFO  c.m.s.batch.item.writer.BatchWriter - Wrote 100 documents to MarkLogic
    18:20:14.394 [ThreadPoolTaskExecutor-5] INFO  c.m.s.batch.item.writer.BatchWriter - Wrote 100 documents to MarkLogic
    18:20:14.394 [ThreadPoolTaskExecutor-7] INFO  c.m.s.batch.item.writer.BatchWriter - Wrote 100 documents to MarkLogic
    18:20:14.394 [ThreadPoolTaskExecutor-4] INFO  c.m.s.batch.item.writer.BatchWriter - Wrote 100 documents to MarkLogic
    18:20:14.394 [ThreadPoolTaskExecutor-1] INFO  c.m.s.batch.item.writer.BatchWriter - Wrote 100 documents to MarkLogic
    18:20:14.394 [ThreadPoolTaskExecutor-6] INFO  c.m.s.batch.item.writer.BatchWriter - Wrote 100 documents to MarkLogic
    18:20:14.394 [ThreadPoolTaskExecutor-9] INFO  c.m.s.batch.item.writer.BatchWriter - Wrote 100 documents to MarkLogic
    18:20:14.394 [ThreadPoolTaskExecutor-8] INFO  c.m.s.batch.item.writer.BatchWriter - Wrote 100 documents to MarkLogic
    18:20:14.394 [ThreadPoolTaskExecutor-10] INFO  c.m.s.batch.item.writer.BatchWriter - Wrote 100 documents to MarkLogic
    18:20:14.394 [main] INFO  c.m.s.b.i.w.ParallelizedMarkLogicItemWriter - Releasing DatabaseClient instances...
    18:20:14.394 [main] INFO  c.m.s.b.i.w.ParallelizedMarkLogicItemWriter - Finished writing data to MarkLogic!

The default configuration is all in gradle.properties. You can modify those on the command line, e.g.

    ./gradlew migrate -Phosts=host1,host2,host3 -PthreadCount=32
    
Or just modify the file and start building your own migration. 

You can also see all the supported arguments:

    ./gradlew help

Comments,questions - please file an issue.
