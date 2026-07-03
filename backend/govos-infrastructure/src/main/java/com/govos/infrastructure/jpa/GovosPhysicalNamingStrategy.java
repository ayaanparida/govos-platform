package com.govos.infrastructure.jpa;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

import java.util.Locale;

/**
 * Converts JPA logical names to PostgreSQL physical names.
 * <ul>
 *   <li>CamelCase → snake_case</li>
 *   <li>All names lowercased</li>
 *   <li>No pluralization</li>
 * </ul>
 */
public class GovosPhysicalNamingStrategy implements PhysicalNamingStrategy {

    @Override
    public Identifier toPhysicalCatalogName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return toPhysicalName(identifier);
    }

    @Override
    public Identifier toPhysicalSchemaName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return toPhysicalName(identifier);
    }

    @Override
    public Identifier toPhysicalTableName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return toPhysicalName(identifier);
    }

    @Override
    public Identifier toPhysicalSequenceName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return toPhysicalName(identifier);
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return toPhysicalName(identifier);
    }

    private Identifier toPhysicalName(Identifier identifier) {
        if (identifier == null) {
            return null;
        }
        String snakeCase = toSnakeCase(identifier.getText());
        return Identifier.toIdentifier(snakeCase, identifier.isQuoted());
    }

    /**
     * Converts a CamelCase or PascalCase name to lowercase snake_case.
     */
    public static String toSnakeCase(String name) {
        if (name == null || name.isBlank()) {
            return name;
        }
        return name
                .replaceAll("([a-z])([A-Z])", "$1_$2")
                .replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2")
                .toLowerCase(Locale.ROOT);
    }

}
