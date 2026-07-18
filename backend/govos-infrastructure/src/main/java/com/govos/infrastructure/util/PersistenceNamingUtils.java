package com.govos.infrastructure.util;

import com.govos.infrastructure.jpa.GovosPhysicalNamingStrategy;

/**
 * Persistence utility helpers.
 */
public final class PersistenceNamingUtils {

    private PersistenceNamingUtils() {
    }

  /**
   * Converts a Java field or class name to a database snake_case identifier.
   *
   * @param name CamelCase or PascalCase name
   * @return lowercase snake_case name
   */
  public static String toSnakeCase(String name) {
    return GovosPhysicalNamingStrategy.toSnakeCase(name);
  }

}
