package io.student.rangiffler.jupiter.extension;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Tracing;
import com.microsoft.playwright.impl.junit.BrowserContextExtension;
import io.qameta.allure.Allure;
import io.qameta.allure.AttachmentOptions;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Playwright context tracing for UI tests, attached to Allure only on failure.
 */
public final class TracingExtension implements BeforeEachCallback, AfterEachCallback {
  @Override
  public void beforeEach(ExtensionContext context) {
    if (!hasPage(context)) {
      return;
    }
    BrowserContextExtension.getOrCreateBrowserContext(context).tracing().start(
        new Tracing.StartOptions().setScreenshots(true).setSnapshots(true)
    );
  }

  @Override
  public void afterEach(ExtensionContext context) {
    if (!hasPage(context)) {
      return;
    }
    if (context.getExecutionException().isEmpty()) {
      BrowserContextExtension.getOrCreateBrowserContext(context).tracing().stop();
      return;
    }
    Path zip = Path.of(
        "build/playwright",
        context.getRequiredTestClass().getName() + "." + context.getRequiredTestMethod().getName() + "-trace.zip"
    );
    try {
      Files.createDirectories(zip.getParent());
      BrowserContextExtension.getOrCreateBrowserContext(context)
          .tracing()
          .stop(new Tracing.StopOptions().setPath(zip));
      try (InputStream input = Files.newInputStream(zip)) {
        Allure.attachment(
            "trace.zip",
            "application/zip",
            input,
            AttachmentOptions.withFileExtension(".zip")
        );
      }
    } catch (IOException exception) {
      throw new UncheckedIOException("Cannot save Playwright trace in " + zip, exception);
    }
  }

  private boolean hasPage(ExtensionContext context) {
    if (context.getTestMethod().isEmpty()) {
      return false;
    }
    for (Parameter parameter : context.getRequiredTestMethod().getParameters()) {
      if (parameter.getType() == Page.class) {
        return true;
      }
    }
    return false;
  }
}
