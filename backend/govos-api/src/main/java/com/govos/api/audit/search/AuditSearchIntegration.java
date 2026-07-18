package com.govos.api.audit.search;

import java.util.UUID;

/**
 * SRH-013 extension point for AUD product search synchronization.
 * Audit payloads remain in AUD; this contract covers future searchable audit metadata only.
 */
public interface AuditSearchIntegration {

    void onAuditEventRecorded(UUID auditEventId, UUID organizationId);

    void onAuditEventArchived(UUID auditEventId, UUID organizationId);
}
