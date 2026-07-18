package com.govos.doc.service;

import com.govos.doc.dto.retention.CreateRetentionPolicyRequest;
import com.govos.doc.dto.retention.UpdateRetentionPolicyRequest;
import com.govos.doc.entity.DocumentRetentionPolicy;

import java.util.UUID;

public interface DocumentRetentionPolicyService {

    DocumentRetentionPolicy createPolicy(CreateRetentionPolicyRequest request);

    DocumentRetentionPolicy updatePolicy(UUID id, UpdateRetentionPolicyRequest request);

    void deletePolicy(UUID id);

    DocumentRetentionPolicy restorePolicy(UUID id);

    DocumentRetentionPolicy findPolicy(UUID id);
}
