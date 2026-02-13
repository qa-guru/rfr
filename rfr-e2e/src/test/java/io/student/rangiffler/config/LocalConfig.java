package io.student.rangiffler.config;

public enum LocalConfig implements Config {
  INSTANCE;


  @Override
  public String frontUrl() {
    return "http://localhost:3001";
  }

  @Override
  public String authJdbcUrl() {
    return "jdbc:mysql://localhost:3306/rangiffler-auth";
  }

  @Override
  public String apiJdbcUrl() {
    return "jdbc:mysql://localhost:3306/rangiffler-api";
  }

  @Override
  public String dbUsername() {
    return "root";
  }

  @Override
  public String dbPassword() {
    return "secret";
  }

  @Override
  public String ghUrl() {
    return "http://api.github.com/";
  }
}
