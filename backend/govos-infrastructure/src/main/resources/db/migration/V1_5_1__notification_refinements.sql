-- =============================================================================
-- GovOS Flyway Migration V1.5.1 — Notification Refinements
-- =============================================================================
-- Scope: Template variables, delivery status MDM prep, retry policy fields
-- =============================================================================

-- Template variable declarations (JSON array of names used in {{placeholders}})
ALTER TABLE govos.ntf_notification_template
    ADD COLUMN template_variables TEXT;

COMMENT ON COLUMN govos.ntf_notification_template.subject_template IS
    'Subject with {{variableName}} placeholders';
COMMENT ON COLUMN govos.ntf_notification_template.body_template IS
    'Body with {{variableName}} placeholders (e.g. Hello {{firstName}})';
COMMENT ON COLUMN govos.ntf_notification_template.template_variables IS
    'JSON array of declared variable names matching {{placeholders}} in templates';

COMMENT ON COLUMN govos.ntf_notification_delivery.delivery_status IS
    'Delivery lifecycle — future MDM type DELIVERY_STATUS (PENDING, QUEUED, SENT, DELIVERED, FAILED, CANCELLED)';

-- Retry policy on queue entries
ALTER TABLE govos.ntf_notification_queue
    RENAME COLUMN next_execution TO next_retry_at;

ALTER TABLE govos.ntf_notification_queue
    ADD COLUMN max_retry INTEGER NOT NULL DEFAULT 3;

DROP INDEX IF EXISTS govos.idx_ntf_notification_queue_next_execution;

CREATE INDEX idx_ntf_notification_queue_next_retry_at
    ON govos.ntf_notification_queue (next_retry_at)
    WHERE deleted = FALSE AND next_retry_at IS NOT NULL;

COMMENT ON COLUMN govos.ntf_notification_queue.retry_count IS 'Current retry attempt count';
COMMENT ON COLUMN govos.ntf_notification_queue.max_retry IS 'Maximum retry attempts before failure';
COMMENT ON COLUMN govos.ntf_notification_queue.next_retry_at IS 'Scheduled time for next retry (no engine yet)';

-- Retry policy on delivery records
ALTER TABLE govos.ntf_notification_delivery
    RENAME COLUMN attempt_count TO retry_count;

ALTER TABLE govos.ntf_notification_delivery
    ADD COLUMN max_retry INTEGER NOT NULL DEFAULT 3,
    ADD COLUMN next_retry_at TIMESTAMPTZ;

CREATE INDEX idx_ntf_notification_delivery_next_retry_at
    ON govos.ntf_notification_delivery (next_retry_at)
    WHERE deleted = FALSE AND next_retry_at IS NOT NULL;

COMMENT ON COLUMN govos.ntf_notification_delivery.retry_count IS 'Current delivery retry attempt count';
COMMENT ON COLUMN govos.ntf_notification_delivery.max_retry IS 'Maximum delivery retries before FAILED';
COMMENT ON COLUMN govos.ntf_notification_delivery.next_retry_at IS 'Scheduled time for next delivery retry';
