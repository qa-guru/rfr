package io.student.rangiffler.config;

import java.util.Locale;

public final class TestConfig {
  private TestConfig() {
  }

  public static String frontUrl() {
    return value("frontUrl", "http://localhost:3001");
  }

  public static String authUrl() {
    return value("authUrl", "http://localhost:9001");
  }

  public static String apiUrl() {
    return value("apiUrl", "http://localhost:8081");
  }

  public static String dbUrl(String schema) {
    return value("dbUrl", "jdbc:mysql://localhost:3306") + "/" + schema
        + "?serverTimezone=UTC&characterEncoding=UTF-8&connectionCollation=utf8mb4_unicode_ci";
  }

  public static String dbUser() {
    return value("dbUser", "root");
  }

  public static String dbPassword() {
    return value("dbPassword", "secret");
  }

  public static String browser() {
    return value("browser", "chromium");
  }

  public static boolean headless() {
    return Boolean.parseBoolean(value("headless", "true"));
  }

  public static int viewportWidth() {
    return Integer.parseInt(value("viewportWidth", "1920"));
  }

  public static int viewportHeight() {
    return Integer.parseInt(value("viewportHeight", "1080"));
  }

  public static int actionTimeout() {
    return Integer.parseInt(value("actionTimeout", "5000"));
  }

  public static int navigateTimeout() {
    return Integer.parseInt(value("navigateTimeout", "30000"));
  }

  public static int assertionTimeout() {
    return Integer.parseInt(value("assertionTimeout", "5000"));
  }

  public static boolean standCheck() {
    return Boolean.parseBoolean(value("standCheck", "true"));
  }

  public static boolean schemaCheck() {
    return Boolean.parseBoolean(value("schemaCheck", "true"));
  }

  public static long standTimeout() {
    return Long.parseLong(value("standTimeout", "180"));
  }

  public static String commit() {
    return value("commit", "");
  }

  static String value(String key, String fallback) {
    String property = System.getProperty("rfr." + key);
    if (property != null && !property.isBlank()) {
      return property;
    }
    String variable = System.getenv("RFR_" + key.replaceAll("([A-Z])", "_$1").toUpperCase(Locale.ROOT));
    if (variable != null && !variable.isBlank()) {
      return variable;
    }
    return fallback;
  }
}
