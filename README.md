[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.sabomichal/jooq-meta-postgres-flyway/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.sabomichal/jooq-meta-postgres-flyway) ![Java CI with Maven](https://github.com/sabomichal/jooq-meta-postgres-flyway/workflows/Java%20CI%20with%20Maven/badge.svg)
# jooq-meta-postgres-flyway
#### the jOOQ PostgreSQL DDL database with Flyway migrations
This package provides a jOOQ meta data source that spins up a PostgreSQL databse running inside docker container using Testcontainers, migrates the database schema using Flyway before reverse engineering the outcome by jOOQ code generator.
