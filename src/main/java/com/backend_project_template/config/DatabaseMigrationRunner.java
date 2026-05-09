package com.backend_project_template.config;

import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Runs safe database migrations on startup.
 * Converts ENUM columns to VARCHAR to support new values without schema
 * changes.
 */
@Component
@Order(1)
public class DatabaseMigrationRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseMigrationRunner.class);

    private final JdbcTemplate jdbcTemplate;

    public DatabaseMigrationRunner(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void run(String... args) {
        migrateColumn("auth_provider", "VARCHAR(20)", "EMAIL");
        migrateColumn("profile_status", "VARCHAR(30)", "PROFILE_INCOMPLETE");
    }

    private void migrateColumn(String columnName, String targetType, String defaultValue) {
        try {
            String currentType = jdbcTemplate.queryForObject(
                    "SELECT COLUMN_TYPE FROM information_schema.COLUMNS " +
                            "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user' AND COLUMN_NAME = ?",
                    String.class,
                    columnName);

            if (currentType != null && currentType.toLowerCase().startsWith("enum")) {
                log.info("Migrating column `{}` from {} to {} ...", columnName, currentType, targetType);
                jdbcTemplate.execute(
                        "ALTER TABLE `user` MODIFY COLUMN `" + columnName + "` " + targetType + " DEFAULT '"
                                + defaultValue + "'");
                log.info("Column `{}` migrated successfully.", columnName);
            } else {
                log.debug("Column `{}` is already {} — no migration needed.", columnName, currentType);
            }
        } catch (Exception e) {
            log.warn("Could not migrate column `{}`: {}", columnName, e.getMessage());
        }
    }
}
