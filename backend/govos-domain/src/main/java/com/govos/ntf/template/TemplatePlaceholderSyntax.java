package com.govos.ntf.template;

/**
 * Placeholder syntax for notification templates.
 * <p>
 * Example body:
 * {@code Hello {{firstName}}, your complaint {{complaintNumber}} has been assigned to {{officerName}}.}
 */
public final class TemplatePlaceholderSyntax {

    public static final String PLACEHOLDER_PREFIX = "{{";
    public static final String PLACEHOLDER_SUFFIX = "}}";

    /**
     * Matches {@code {{variableName}}} placeholders (variable name: letters, digits, underscore).
     */
    public static final String PLACEHOLDER_PATTERN = "\\{\\{([a-zA-Z][a-zA-Z0-9_]*)\\}\\}";

    private TemplatePlaceholderSyntax() {
    }
}
