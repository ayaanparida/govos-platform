-- =============================================================================
-- GovOS Flyway Migration V1.1.0 — Master Data (MDM)
-- =============================================================================
-- Domain    : MDM (Master Data Management)
-- Table     : govos.mdm_master_data
-- ADR       : ADR-002 Modular Monolith, ADR-008 Flyway
-- =============================================================================

CREATE TABLE govos.mdm_master_data (
    id              UUID            NOT NULL,
    code            VARCHAR(100),
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted         BOOLEAN         NOT NULL DEFAULT FALSE,
    version         BIGINT          NOT NULL DEFAULT 0,
    created_by      VARCHAR(100)    NOT NULL,
    created_date    TIMESTAMPTZ     NOT NULL,
    updated_by      VARCHAR(100)    NOT NULL,
    updated_date    TIMESTAMPTZ     NOT NULL,
    type            VARCHAR(100)    NOT NULL,
    data_key        VARCHAR(200)    NOT NULL,
    data_value      VARCHAR(500)    NOT NULL,
    description     VARCHAR(1000),
    display_order   INTEGER,
    system_defined  BOOLEAN         NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_mdm_master_data PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uk_mdm_master_data_type_key_active
    ON govos.mdm_master_data (type, data_key)
    WHERE deleted = FALSE;

CREATE INDEX idx_mdm_master_data_type_active
    ON govos.mdm_master_data (type)
    WHERE deleted = FALSE;

COMMENT ON TABLE govos.mdm_master_data IS 'Master reference data — lookup values, configuration codes, and enumerations';
COMMENT ON COLUMN govos.mdm_master_data.type IS 'Master data category (e.g. COMPLAINT_STATUS, DOCUMENT_TYPE)';
COMMENT ON COLUMN govos.mdm_master_data.data_key IS 'Unique key within the type category';
COMMENT ON COLUMN govos.mdm_master_data.data_value IS 'Display or stored value for the key';
COMMENT ON COLUMN govos.mdm_master_data.system_defined IS 'When true, record is protected from modification and deletion';
