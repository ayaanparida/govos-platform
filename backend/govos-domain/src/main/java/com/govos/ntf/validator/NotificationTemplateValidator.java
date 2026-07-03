package com.govos.ntf.validator;

import com.govos.ntf.dto.CreateNotificationTemplateRequest;
import com.govos.ntf.dto.UpdateNotificationTemplateRequest;
import com.govos.ntf.exception.DuplicateCodeException;
import com.govos.ntf.exception.InvalidTemplateVariableException;
import com.govos.ntf.repository.NotificationTemplateRepository;
import com.govos.ntf.template.TemplateVariableExtractor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class NotificationTemplateValidator {

    private final NotificationTemplateRepository notificationTemplateRepository;

    public NotificationTemplateValidator(NotificationTemplateRepository notificationTemplateRepository) {
        this.notificationTemplateRepository = notificationTemplateRepository;
    }

    public void validateCreate(CreateNotificationTemplateRequest request) {
        if (notificationTemplateRepository.existsByCodeAndDeletedFalse(request.code())) {
            throw new DuplicateCodeException("NotificationTemplate", request.code());
        }
        validateTemplateVariables(request.subjectTemplate(), request.bodyTemplate(), request.templateVariables());
    }

    public void validateUpdate(UUID id, UpdateNotificationTemplateRequest request) {
        notificationTemplateRepository.findByCodeAndDeletedFalse(request.code())
                .filter(entity -> !entity.getId().equals(id))
                .ifPresent(entity -> {
                    throw new DuplicateCodeException("NotificationTemplate", request.code());
                });
        validateTemplateVariables(request.subjectTemplate(), request.bodyTemplate(), request.templateVariables());
    }

    private void validateTemplateVariables(
            String subjectTemplate,
            String bodyTemplate,
            java.util.List<String> templateVariables) {
        try {
            TemplateVariableExtractor.validateDeclaredVariables(
                    templateVariables, subjectTemplate, bodyTemplate);
        } catch (IllegalArgumentException e) {
            throw new InvalidTemplateVariableException(e.getMessage());
        }
    }
}
