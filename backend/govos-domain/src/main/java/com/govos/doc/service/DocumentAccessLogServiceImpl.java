package com.govos.doc.service;

import com.govos.idm.entity.User;
import com.govos.idm.exception.UserNotFoundException;
import com.govos.idm.repository.UserRepository;
import com.govos.doc.dto.CreateDocumentAccessLogRequest;
import com.govos.doc.dto.DocumentAccessLogDto;
import com.govos.doc.entity.Document;
import com.govos.doc.entity.DocumentAccessLog;
import com.govos.doc.entity.DocumentAccessAction;
import com.govos.doc.exception.DocumentAccessLogNotFoundException;
import com.govos.doc.exception.DocumentNotFoundException;
import com.govos.doc.mapper.DocumentAccessLogMapper;
import com.govos.doc.repository.DocumentAccessLogRepository;
import com.govos.doc.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DocumentAccessLogServiceImpl implements DocumentAccessLogService {

    private final DocumentAccessLogRepository documentAccessLogRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final DocumentAccessLogMapper documentAccessLogMapper;

    public DocumentAccessLogServiceImpl(
            DocumentAccessLogRepository documentAccessLogRepository,
            DocumentRepository documentRepository,
            UserRepository userRepository,
            DocumentAccessLogMapper documentAccessLogMapper) {
        this.documentAccessLogRepository = documentAccessLogRepository;
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.documentAccessLogMapper = documentAccessLogMapper;
    }

    @Override
    public DocumentAccessLogDto getById(UUID id) {
        return documentAccessLogMapper.toDto(findActiveById(id));
    }

    @Override
    public List<DocumentAccessLogDto> getByDocumentId(UUID documentId) {
        return documentAccessLogRepository.findByDocument_IdAndDeletedFalseOrderByAccessedAtDesc(documentId).stream()
                .map(documentAccessLogMapper::toDto)
                .toList();
    }

    @Override
    public List<DocumentAccessLogDto> getByUserId(UUID userId) {
        return documentAccessLogRepository.findByUser_IdAndDeletedFalseOrderByAccessedAtDesc(userId).stream()
                .map(documentAccessLogMapper::toDto)
                .toList();
    }

    @Override
    public List<DocumentAccessLogDto> getByDocumentIdAndAction(UUID documentId, DocumentAccessAction action) {
        return documentAccessLogRepository
                .findByDocument_IdAndActionAndDeletedFalseOrderByAccessedAtDesc(documentId, action).stream()
                .map(documentAccessLogMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public DocumentAccessLogDto create(CreateDocumentAccessLogRequest request) {
        Document document = documentRepository.findByIdAndDeletedFalse(request.documentId())
                .orElseThrow(() -> new DocumentNotFoundException(request.documentId()));
        User user = userRepository.findByIdAndDeletedFalse(request.userId())
                .orElseThrow(() -> new UserNotFoundException(request.userId()));

        DocumentAccessLog entity = new DocumentAccessLog();
        entity.setCode(request.code());
        entity.setDocument(document);
        entity.setUser(user);
        entity.setAction(request.action());
        entity.setAccessedAt(request.accessedAt() != null ? request.accessedAt() : Instant.now());
        entity.setActive(true);
        entity.setDeleted(false);

        return documentAccessLogMapper.toDto(documentAccessLogRepository.save(entity));
    }

    private DocumentAccessLog findActiveById(UUID id) {
        return documentAccessLogRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new DocumentAccessLogNotFoundException(id));
    }
}
