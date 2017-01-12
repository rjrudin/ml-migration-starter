This is a starter kit for creating your own project that uses [Spring Batch](http://projects.spring.io/spring-batch/) and
[marklogic-spring-batch](https://github.com/sastafford/marklogic-spring-batch) for migrating data from any RDBMS into MarkLogic. You 
can clone/fork/do whatever you want with this repository to get your own project going.

This project has the following defaults in place that you can use as a starting point:

1. Talks to a local MySQL database with the [Sakila](https://dev.mysql.com/doc/sakila/en/) database loaded in it, using the MySQL JDBC driver
1. Uses Spring's ColumnMapReader to turn every row into a Map<String, Object>
1. Uses StAX to turn that Map<String, Object> into a simple XML document
1. Uses the ML Java API to write that XML document to ML (Coming soon - support for multiple DatabaseClient instances and a thread pool to speed this up)
1. Defaults to writing to localhost/8000/admin/admin
1. Has a Gradle task for launching the migration - "./gradlew migrate"
