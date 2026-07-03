-- =============================================================================
-- GovOS Flyway Migration V1.3.0 — Organization (ORG)
-- =============================================================================
-- Domain    : ORG (Organization Management)
-- ADR       : ADR-002 Modular Monolith, ADR-008 Flyway
-- =============================================================================

-- ---------------------------------------------------------------------------
-- org_organization
-- ---------------------------------------------------------------------------
CREATE TABLE govos.org_organization (
    id                      UUID            NOT NULL,
    code                    VARCHAR(100)    NOT NULL,
    active                  BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted                 BOOLEAN         NOT NULL DEFAULT FALSE,
    version                 BIGINT          NOT NULL DEFAULT 0,
    created_by              VARCHAR(100)    NOT NULL,
    created_date            TIMESTAMPTZ     NOT NULL,
    updated_by              VARCHAR(100)    NOT NULL,
    updated_date            TIMESTAMPTZ     NOT NULL,
    name                    VARCHAR(255)    NOT NULL,
    short_name              VARCHAR(50),
    type                    VARCHAR(50),
    registration_number     VARCHAR(100),
    email                   VARCHAR(255),
    phone                   VARCHAR(20),
    website                 VARCHAR(500),
    status                  VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    CONSTRAINT pk_org_organization PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uk_org_organization_code_active
    ON govos.org_organization (code)
    WHERE deleted = FALSE;

-- ---------------------------------------------------------------------------
-- org_department
-- ---------------------------------------------------------------------------
CREATE TABLE govos.org_department (
    id                      UUID            NOT NULL,
    code                    VARCHAR(100)    NOT NULL,
    active                  BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted                 BOOLEAN         NOT NULL DEFAULT FALSE,
    version                 BIGINT          NOT NULL DEFAULT 0,
    created_by              VARCHAR(100)    NOT NULL,
    created_date            TIMESTAMPTZ     NOT NULL,
    updated_by              VARCHAR(100)    NOT NULL,
    updated_date            TIMESTAMPTZ     NOT NULL,
    organization_id         UUID            NOT NULL,
    parent_department_id    UUID,
    name                    VARCHAR(255)    NOT NULL,
    description             VARCHAR(1000),
    CONSTRAINT pk_org_department PRIMARY KEY (id),
    CONSTRAINT fk_org_department_organization FOREIGN KEY (organization_id) REFERENCES govos.org_organization (id),
    CONSTRAINT fk_org_department_parent FOREIGN KEY (parent_department_id) REFERENCES govos.org_department (id)
);

CREATE UNIQUE INDEX uk_org_department_org_code_active
    ON govos.org_department (organization_id, code)
    WHERE deleted = FALSE;

CREATE INDEX idx_org_department_organization_id
    ON govos.org_department (organization_id)
    WHERE deleted = FALSE;

-- ---------------------------------------------------------------------------
-- org_office
-- ---------------------------------------------------------------------------
CREATE TABLE govos.org_office (
    id              UUID            NOT NULL,
    code            VARCHAR(100)    NOT NULL,
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted         BOOLEAN         NOT NULL DEFAULT FALSE,
    version         BIGINT          NOT NULL DEFAULT 0,
    created_by      VARCHAR(100)    NOT NULL,
    created_date    TIMESTAMPTZ     NOT NULL,
    updated_by      VARCHAR(100)    NOT NULL,
    updated_date    TIMESTAMPTZ     NOT NULL,
    department_id   UUID            NOT NULL,
    office_name     VARCHAR(255)    NOT NULL,
    address         VARCHAR(1000),
    district        VARCHAR(100),
    state           VARCHAR(100),
    latitude        NUMERIC(10, 7),
    longitude       NUMERIC(10, 7),
    CONSTRAINT pk_org_office PRIMARY KEY (id),
    CONSTRAINT fk_org_office_department FOREIGN KEY (department_id) REFERENCES govos.org_department (id)
);

CREATE UNIQUE INDEX uk_org_office_dept_code_active
    ON govos.org_office (department_id, code)
    WHERE deleted = FALSE;

-- ---------------------------------------------------------------------------
-- org_designation
-- ---------------------------------------------------------------------------
CREATE TABLE govos.org_designation (
    id              UUID            NOT NULL,
    code            VARCHAR(100)    NOT NULL,
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted         BOOLEAN         NOT NULL DEFAULT FALSE,
    version         BIGINT          NOT NULL DEFAULT 0,
    created_by      VARCHAR(100)    NOT NULL,
    created_date    TIMESTAMPTZ     NOT NULL,
    updated_by      VARCHAR(100)    NOT NULL,
    updated_date    TIMESTAMPTZ     NOT NULL,
    title           VARCHAR(200)    NOT NULL,
    grade           VARCHAR(50),
    description     VARCHAR(1000),
    CONSTRAINT pk_org_designation PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uk_org_designation_code_active
    ON govos.org_designation (code)
    WHERE deleted = FALSE;

-- ---------------------------------------------------------------------------
-- org_employee
-- ---------------------------------------------------------------------------
CREATE TABLE govos.org_employee (
    id                  UUID            NOT NULL,
    code                VARCHAR(100),
    active              BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted             BOOLEAN         NOT NULL DEFAULT FALSE,
    version             BIGINT          NOT NULL DEFAULT 0,
    created_by          VARCHAR(100)    NOT NULL,
    created_date        TIMESTAMPTZ     NOT NULL,
    updated_by          VARCHAR(100)    NOT NULL,
    updated_date        TIMESTAMPTZ     NOT NULL,
    user_id             UUID            NOT NULL,
    organization_id     UUID            NOT NULL,
    department_id       UUID            NOT NULL,
    office_id           UUID,
    designation_id      UUID            NOT NULL,
    employee_number     VARCHAR(50)     NOT NULL,
    joining_date        DATE,
    retirement_date     DATE,
    official_email      VARCHAR(255),
    official_mobile     VARCHAR(20),
    CONSTRAINT pk_org_employee PRIMARY KEY (id),
    CONSTRAINT fk_org_employee_user FOREIGN KEY (user_id) REFERENCES govos.idm_user (id),
    CONSTRAINT fk_org_employee_organization FOREIGN KEY (organization_id) REFERENCES govos.org_organization (id),
    CONSTRAINT fk_org_employee_department FOREIGN KEY (department_id) REFERENCES govos.org_department (id),
    CONSTRAINT fk_org_employee_office FOREIGN KEY (office_id) REFERENCES govos.org_office (id),
    CONSTRAINT fk_org_employee_designation FOREIGN KEY (designation_id) REFERENCES govos.org_designation (id)
);

CREATE UNIQUE INDEX uk_org_employee_number_active
    ON govos.org_employee (employee_number)
    WHERE deleted = FALSE;

CREATE INDEX idx_org_employee_user_id
    ON govos.org_employee (user_id)
    WHERE deleted = FALSE;

CREATE INDEX idx_org_employee_organization_id
    ON govos.org_employee (organization_id)
    WHERE deleted = FALSE;

-- ---------------------------------------------------------------------------
-- org_user_organization
-- ---------------------------------------------------------------------------
CREATE TABLE govos.org_user_organization (
    id                      UUID            NOT NULL,
    code                    VARCHAR(100),
    active                  BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted                 BOOLEAN         NOT NULL DEFAULT FALSE,
    version                 BIGINT          NOT NULL DEFAULT 0,
    created_by              VARCHAR(100)    NOT NULL,
    created_date            TIMESTAMPTZ     NOT NULL,
    updated_by              VARCHAR(100)    NOT NULL,
    updated_date            TIMESTAMPTZ     NOT NULL,
    user_id                 UUID            NOT NULL,
    organization_id         UUID            NOT NULL,
    default_organization    BOOLEAN         NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_org_user_organization PRIMARY KEY (id),
    CONSTRAINT fk_org_user_organization_user FOREIGN KEY (user_id) REFERENCES govos.idm_user (id),
    CONSTRAINT fk_org_user_organization_org FOREIGN KEY (organization_id) REFERENCES govos.org_organization (id)
);

CREATE UNIQUE INDEX uk_org_user_organization_user_org_active
    ON govos.org_user_organization (user_id, organization_id)
    WHERE deleted = FALSE;

CREATE UNIQUE INDEX uk_org_user_organization_default_active
    ON govos.org_user_organization (user_id)
    WHERE deleted = FALSE AND default_organization = TRUE;

-- ---------------------------------------------------------------------------
-- org_department_hierarchy
-- ---------------------------------------------------------------------------
CREATE TABLE govos.org_department_hierarchy (
    id                      UUID            NOT NULL,
    code                    VARCHAR(100),
    active                  BOOLEAN         NOT NULL DEFAULT TRUE,
    deleted                 BOOLEAN         NOT NULL DEFAULT FALSE,
    version                 BIGINT          NOT NULL DEFAULT 0,
    created_by              VARCHAR(100)    NOT NULL,
    created_date            TIMESTAMPTZ     NOT NULL,
    updated_by              VARCHAR(100)    NOT NULL,
    updated_date            TIMESTAMPTZ     NOT NULL,
    parent_department_id    UUID            NOT NULL,
    child_department_id     UUID            NOT NULL,
    CONSTRAINT pk_org_department_hierarchy PRIMARY KEY (id),
    CONSTRAINT fk_org_dept_hierarchy_parent FOREIGN KEY (parent_department_id) REFERENCES govos.org_department (id),
    CONSTRAINT fk_org_dept_hierarchy_child FOREIGN KEY (child_department_id) REFERENCES govos.org_department (id),
    CONSTRAINT chk_org_dept_hierarchy_not_self CHECK (parent_department_id <> child_department_id)
);

CREATE UNIQUE INDEX uk_org_dept_hierarchy_parent_child_active
    ON govos.org_department_hierarchy (parent_department_id, child_department_id)
    WHERE deleted = FALSE;

COMMENT ON TABLE govos.org_organization IS 'Government departments, agencies, and organizational units';
COMMENT ON TABLE govos.org_department IS 'Departments within an organization';
COMMENT ON TABLE govos.org_office IS 'Physical office locations under a department';
COMMENT ON TABLE govos.org_designation IS 'Employee designations and grades';
COMMENT ON TABLE govos.org_employee IS 'Employee postings linking users to org structure';
COMMENT ON TABLE govos.org_user_organization IS 'User membership and default organization context';
COMMENT ON TABLE govos.org_department_hierarchy IS 'Explicit parent-child department hierarchy edges';
