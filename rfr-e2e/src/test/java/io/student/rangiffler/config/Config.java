package io.student.rangiffler.config;

public interface Config {
    static Config getInstance() {
        return LocalConfig.INSTANCE;
    }
    String frontUrl();
    String authJdbcUrl();
    String apiJdbcUrl();
    String dbUsername();
    String dbPassword();
    String ghUrl();
}
