-- =============================================================================
-- GovOS Flyway Migration V1.3.1 — Organization Refinements
-- =============================================================================
-- Scope: Employee number sequence, MDM organization type, GIS prep
-- =============================================================================

-- Employee number sequence (format EMP-YYYY-NNNNNN)
CREATE TABLE govos.org_employee_number_sequence (
    year            INTEGER         NOT NULL,
    last_sequence   BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT pk_org_employee_number_sequence PRIMARY KEY (year)
);

COMMENT ON TABLE govos.org_employee_number_sequence IS
    'Yearly sequence for business employee numbers (EMP-YYYY-NNNNNN); UUID remains PK on org_employee';

-- Widen organization type for MDM reference keys (e.g. STATE_GOVERNMENT)
ALTER TABLE govos.org_organization
    ALTER COLUMN type TYPE VARCHAR(100);

COMMENT ON COLUMN govos.org_organization.type IS
    'MDM reference key — master data type ORGANIZATION_TYPE (e.g. STATE_GOVERNMENT, DISTRICT)';

COMMENT ON COLUMN govos.org_office.latitude IS 'WGS84 latitude — prepared for GIS integration';
COMMENT ON COLUMN govos.org_office.longitude IS 'WGS84 longitude — prepared for GIS integration';

CREATE INDEX idx_org_office_geo_coordinates
    ON govos.org_office (latitude, longitude)
    WHERE deleted = FALSE AND latitude IS NOT NULL AND longitude IS NOT NULL;
