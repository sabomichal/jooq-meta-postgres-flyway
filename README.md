[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.sabomichal/jooq-meta-postgres-flyway/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.sabomichal/jooq-meta-postgres-flyway) ![Java CI with Maven](https://github.com/sabomichal/jooq-meta-postgres-flyway/workflows/Java%20CI%20with%20Maven/badge.svg)
# jooq-meta-postgres-flyway
#### the jOOQ PostgreSQL DDL database with Flyway migrations
This package provides a jOOQ meta data source that spins up a PostgreSQL database running inside docker container using Testcontainers, migrates the database schema using Flyway before reverse engineering the outcome by jOOQ code generator.

## jOOQ version
Plugin is built against jOOQ 3.20.x which itself works with Java 21 and higher. For Java 11 and jOOQ 3.16.x please use version 1.0.x of the plugin.

## Usage
### Properties
Plugin can be further customized with these additional optional properties:
#### locations
Used directly as the Flyway locations property. Comma-separated list of locations to scan recursively for migrations. Defaults to empty.
#### dockerImage
Custom Docker image name used as compatible substitute for default image name "postgres:14".
#### placeholders
Used as the Flyway placeholders property. Comma-separated list of key-value pairs in a form of "key=value". Defaults to empty map.
#### flyway.postgresql.transactional.lock
Boolean flag for enabling or disabling the `PostgreSQL` transactional locks, which were enabled by default in `flyway-core` version `9.1.2` and higher.
See https://github.com/flyway/flyway/issues/3492 for more details. Defaults to `true`
### Maven
Simply add the meta plugin as a dependency to jOOQ codegen maven plugin. The following example demonstrates the usage.
```xml
<plugin>
    <groupId>org.jooq</groupId>
    <artifactId>jooq-codegen-maven</artifactId>
    <dependencies>
        <dependency>
            <groupId>com.github.sabomichal</groupId>
            <artifactId>jooq-meta-postgres-flyway</artifactId>
            <version>${plugin.version}</version>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-database-postgresql</artifactId>
            <version>${flyway.version}</version>
        </dependency>
    </dependencies>
    <executions>
        <execution>
            <id>jooq-codegen</id>
            <phase>generate-sources</phase>
            <goals>
                <goal>generate</goal>
            </goals>
            <configuration>
                <generator>
                    <database>
                        <name>com.github.sabomichal.jooq.PostgresDDLDatabase</name>
                        <properties>
                            <property>
                                <key>locations</key>
                                <value>src/main/resources/db/migration</value>
                                <key>dockerImage</key>
                                <value>postgres:14</value>
                                <key>placeholders</key>
                                <value>a=1,b=2</value>
                                <key>flyway.postgresql.transactional.lock</key>
                                <value>true</value>
                            </property>
                        </properties>
                        <includes>public.*</includes>
                        <excludes>flyway_schema_history</excludes>
                        <inputSchema>public</inputSchema>
                    </database>
                    <generate>
                        ...
                    </generate>
                </generator>
            </configuration>
        </execution>
    </executions>
</plugin>
```
### Gradle
```groovy
dependencies {
    // ...
    jooqCodegen("org.flywaydb:flyway-database-postgresql")
    jooqGenerator "com.github.sabomichal:jooq-meta-postgres-flyway:${plugin.version}"
    // ...
}

jooq {
    configurations {
        main {
            generationTool {
                generator {
                    database {
                        name = "com.github.sabomichal.jooq.PostgresDDLDatabase"
                        inputSchema = "public"
                        includes = "public.*"
                        excludes = "flyway_schema_history"
                        properties {
                            property {
                                key = "locations"
                                value = "src/main/resources/db/migration"
                            }
                            property {
                                key = "dockerImage"
                                value = "postgres:14"
                            }
                            property {
                                key = "placeholders"
                                value = "a=1,b=2"
                            }
                        }
                    }
                    generate {
                        // ...
                    }
                }
            }
        }
    }
}
```
If you like it, give it a star, if you don't, write an issue.
