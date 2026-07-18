-- =============================================================================
-- GovOS Flyway Migration V1 — Baseline
-- =============================================================================
-- Scope     : Infrastructure only (no business tables)
-- Schema    : govos
-- ADR       : ADR-008 Flyway for Database Migrations
-- =============================================================================

-- UUID generation support (gen_random_uuid)
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Application schema
CREATE SCHEMA IF NOT EXISTS govos;

COMMENT ON SCHEMA govos IS 'GovOS application schema — all domain tables reside here';

-- Default search path for this database session context
-- Domain migrations (V2+) will create tables within the govos schema
