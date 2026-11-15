package io.student.rangiffler.jupiter.extension;

import org.junit.jupiter.api.extension.ExtensionContext;

public class DemoSuiteExtension implements SuiteExtension{
  @Override
  public void beforeSuite(ExtensionContext context) {
    System.out.println("### BEFORE ALL TESTS");
  }

  @Override
  public void afterSuite(ExtensionContext context) {
    System.out.println("### AFTER ALL TESTS");
  }
}
