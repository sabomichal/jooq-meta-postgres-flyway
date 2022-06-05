package com.github.sabomichal.jooq;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.Location;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.jooq.meta.postgres.PostgresDatabase;
import org.jooq.tools.JooqLogger;
import org.jooq.tools.jdbc.JDBCUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;


import static org.jooq.tools.StringUtils.isBlank;


/**
 * The PostgreSQL DDL database.
 * <p>
 * This meta data source spins up a PostgreSQL databse running inside docker container,
 * migrates the database schema using Flyway before reverse engineering the outcome.
 * <p>
 * The SQL migrations are located in the <code>locations</code> property
 * available from {@link #getProperties()}.
 *
 * @author Lukas Eder
 * @author Michal Sabo
 */
public class PostgresDDLDatabase extends PostgresDatabase {

    private static final JooqLogger log = JooqLogger.getLogger(PostgresDDLDatabase.class);
    private static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse("postgres");
    private static final String DEFAULT_TAG = "14";
    private static final String KEY_VALUE_SEPARATOR = "=";

    private Connection connection;
    private PostgreSQLContainer<?> postgresContainer;

    @Override
    protected DSLContext create0() {
        return DSL.using(connection(), SQLDialect.POSTGRES);
    }

    /**
     * Accessor to the connection that has been initialised by this database.
     */
    protected Connection connection() {
        if (connection == null) {
            try {
                final String customDockerImageName = getProperties().getProperty("dockerImage");
                DockerImageName dockerImageName;
                if (isBlank(customDockerImageName)) {
                    dockerImageName = DEFAULT_IMAGE_NAME.withTag(DEFAULT_TAG);
                } else {
                    dockerImageName = DockerImageName.parse(customDockerImageName).asCompatibleSubstituteFor("postgres");
                }

                postgresContainer = new PostgreSQLContainer<>(dockerImageName)
                    .withDatabaseName("jooqdb")
                    .withUsername("user")
                    .withPassword("pwd");
                postgresContainer.start();

                Properties info = new Properties();
                info.put("user", postgresContainer.getUsername());
                info.put("password", postgresContainer.getPassword());
                connection = new org.postgresql.Driver().connect(postgresContainer.getJdbcUrl(), info);

                String locationsProperty = getProperties().getProperty("locations");
                if (isBlank(locationsProperty)) {
                    locationsProperty = "";
                    log.warn("No scripts location defined", "It is recommended that you provide an explicit script directory to scan");
                }
                String[] locations = Arrays.stream(locationsProperty.split(","))
                    .map(l -> Location.FILESYSTEM_PREFIX + getBasedir() + "/" + l)
                    .toArray(String[]::new);

                Map<String, String> placeholders;
                String placeholdersProperty = getProperties().getProperty("placeholders");
                if (isBlank(placeholdersProperty)) {
                    placeholders = Collections.emptyMap();
                } else {
                    placeholders = Arrays.stream(placeholdersProperty.split(","))
                        .map(p -> p.split(KEY_VALUE_SEPARATOR))
                        .collect(Collectors.toMap(pair -> pair[0], pair -> pair[1]));
                }

                String defaultSchema = getProperties().getProperty("defaultSchema");
                if (isBlank(defaultSchema)) {
                    defaultSchema = "public";
                }
                Flyway.configure()
                    .dataSource(postgresContainer.getJdbcUrl(), postgresContainer.getUsername(), postgresContainer.getPassword())
                    .locations(locations)
                    .schemas(defaultSchema)
                    .placeholders(placeholders)
                    .load()
                    .migrate();

                setConnection(connection);

            } catch (Exception e) {
                throw new DataAccessException("Error while exporting schema", e);
            }
        }
        return connection;
    }

    @Override
    public void close() {
        JDBCUtils.safeClose(connection);
        connection = null;
        if (postgresContainer != null) {
            postgresContainer.close();
            postgresContainer = null;
        }
        super.close();
    }
}
