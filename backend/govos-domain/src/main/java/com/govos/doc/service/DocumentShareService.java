package com.govos.doc.service;

import com.govos.doc.dto.share.CreateShareRequest;
import com.govos.doc.entity.DocumentShare;

import java.util.UUID;

public interface DocumentShareService {

    DocumentShare createShare(CreateShareRequest request);

    DocumentShare expireShare(UUID id);

    DocumentShare revokeShare(UUID id);

    DocumentShare findShare(UUID id);
}
