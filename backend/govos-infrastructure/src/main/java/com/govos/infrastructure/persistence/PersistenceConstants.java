package com.govos.infrastructure.persistence;

/**
 * Central constants for GovOS persistence and database conventions.
 */
public final class PersistenceConstants {

    private PersistenceConstants() {
    }

    // -------------------------------------------------------------------------
    // Schema
    // -------------------------------------------------------------------------

    public static final String SCHEMA_NAME = "govos";

    public static final String DEFAULT_CATALOG = "govos";

    // -------------------------------------------------------------------------
    // Flyway
    // -------------------------------------------------------------------------

    public static final String FLYWAY_MIGRATION_LOCATION = "classpath:db/migration";

    public static final String FLYWAY_HISTORY_TABLE = "flyway_schema_history";

    // -------------------------------------------------------------------------
    // Naming conventions (ADR-002, ADR-005, ADR-008)
    // -------------------------------------------------------------------------

    /**
     * Table naming pattern: {@code {domain_prefix}_{entity_name}}.
     * <p>Examples: {@code idm_user}, {@code org_organization}, {@code aud_audit_log}</p>
     */
    public static final String TABLE_NAME_PATTERN = "{domain}_{entity}";

    public static final String COLUMN_NAMING_CONVENTION = "snake_case";

    public static final String TABLE_NAMING_CONVENTION = "snake_case";

    /**
     * Domain table prefixes used across GovOS.
     */
    public static final String PREFIX_IDENTITY = "idm";

    public static final String PREFIX_ORGANIZATION = "org";

    public static final String PREFIX_AUDIT = "aud";

    public static final String PREFIX_COMPLAINT = "cmp";

    public static final String PREFIX_WORKFLOW = "wrk";

    public static final String PREFIX_NOTIFICATION = "ntf";

    // -------------------------------------------------------------------------
    // JPA / Hibernate
    // -------------------------------------------------------------------------

    public static final String ENTITY_SCAN_BASE_PACKAGE = "com.govos";

    public static final String REPOSITORY_SCAN_BASE_PACKAGE = "com.govos";

    public static final int JDBC_BATCH_SIZE = 50;

}
