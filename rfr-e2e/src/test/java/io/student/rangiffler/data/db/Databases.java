package io.student.rangiffler.data.db;

import io.student.rangiffler.config.TestConfig;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

public enum Databases {
  AUTH("rangiffler-auth"),
  API("rangiffler-api");

  private final DataSource dataSource;
  private final PlatformTransactionManager transactionManager;

  Databases(String schema) {
    DriverManagerDataSource dataSource = new DriverManagerDataSource(
        TestConfig.dbUrl(schema),
        TestConfig.dbUser(),
        TestConfig.dbPassword()
    );
    dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
    this.dataSource = dataSource;
    this.transactionManager = new DataSourceTransactionManager(dataSource);
  }

  public DataSource dataSource() {
    return dataSource;
  }

  public PlatformTransactionManager transactionManager() {
    return transactionManager;
  }
}
