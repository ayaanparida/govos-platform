package com.govos.ntf.template;

import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts and validates {@code {{variableName}}} placeholders in template text.
 */
public final class TemplateVariableExtractor {

    private static final Pattern PLACEHOLDER =
            Pattern.compile(TemplatePlaceholderSyntax.PLACEHOLDER_PATTERN);

    private TemplateVariableExtractor() {
    }

    public static Set<String> extractPlaceholders(String... templateParts) {
        Set<String> placeholders = new LinkedHashSet<>();
        if (templateParts == null) {
            return placeholders;
        }
        for (String part : templateParts) {
            if (!StringUtils.hasText(part)) {
                continue;
            }
            Matcher matcher = PLACEHOLDER.matcher(part);
            while (matcher.find()) {
                placeholders.add(matcher.group(1));
            }
        }
        return placeholders;
    }

    public static void validateDeclaredVariables(
            List<String> declaredVariables,
            String subjectTemplate,
            String bodyTemplate) {
        Set<String> placeholders = extractPlaceholders(subjectTemplate, bodyTemplate);
        if (placeholders.isEmpty()) {
            return;
        }
        if (declaredVariables == null || declaredVariables.isEmpty()) {
            throw new IllegalArgumentException(
                    "Template contains placeholders " + placeholders
                            + " but no templateVariables were declared");
        }
        Set<String> declared = new LinkedHashSet<>(declaredVariables);
        for (String placeholder : placeholders) {
            if (!declared.contains(placeholder)) {
                throw new IllegalArgumentException(
                        "Undeclared template placeholder: {{" + placeholder + "}}");
            }
        }
    }
}
