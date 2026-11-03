package io.student.rangiffler.playwright;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.junit.Options;
import com.microsoft.playwright.junit.OptionsFactory;
import io.student.rangiffler.config.TestConfig;

import java.nio.file.Path;

public class PlaywrightOptionsFactory implements OptionsFactory {

  static final Path VIDEO_DIR = Path.of("build/playwright/video");

  @Override
  public Options getOptions() {
    return new Options()
        .setBaseUrl(TestConfig.frontUrl())
        .setHeadless(TestConfig.headless())
        .setBrowserName(TestConfig.browser())
        .setContextOptions(new Browser.NewContextOptions()
            .setViewportSize(1280, 800)
            .setLocale("en-US")
            .setRecordVideoDir(VIDEO_DIR)
            .setRecordVideoSize(1280, 800));
  }
}
