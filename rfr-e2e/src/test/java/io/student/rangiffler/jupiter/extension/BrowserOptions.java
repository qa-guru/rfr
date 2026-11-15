package io.student.rangiffler.jupiter.extension;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.junit.Options;
import com.microsoft.playwright.junit.OptionsFactory;
import io.student.rangiffler.config.TestConfig;

import java.nio.file.Path;

public class BrowserOptions implements OptionsFactory {

  static final Path VIDEO_DIR = Path.of("build/playwright/video");

  @Override
  public Options getOptions() {
    return new Options()
        .setBaseUrl(TestConfig.frontUrl())
        .setHeadless(TestConfig.headless())
        .setBrowserName(TestConfig.browser())
        .setTestIdAttribute("data-test-id")
        .setContextOptions(contextOptions());
  }

  private Browser.NewContextOptions contextOptions() {
    return new Browser.NewContextOptions()
        .setViewportSize(TestConfig.viewportWidth(), TestConfig.viewportHeight())
        .setLocale("en-US")
        .setRecordVideoDir(VIDEO_DIR)
        .setRecordVideoSize(TestConfig.viewportWidth(), TestConfig.viewportHeight());
  }
}
