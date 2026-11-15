package io.student.rangiffler.jupiter.extension;

import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.ArrayList;
import java.util.List;

public final class SuiteRun implements AutoCloseable {
  private final SuiteExtension origin;
  private final ExtensionContext context;
  private final List<RuntimeException> failures;
  private Phase phase;

  SuiteRun(SuiteExtension origin, ExtensionContext context) {
    this.origin = origin;
    this.context = context;
    this.failures = new ArrayList<>();
    this.phase = Phase.READY;
  }

  void start() {
    synchronized (this) {
      if (this.phase == Phase.READY) {
        this.phase = Phase.STARTED;
        try {
          this.origin.beforeSuite(this.context);
        } catch (RuntimeException failure) {
          this.failures.add(failure);
          throw failure;
        }
        return;
      }
      if (!this.failures.isEmpty()) {
        throw this.failures.getFirst();
      }
    }
  }

  @Override
  public void close() {
    synchronized (this) {
      if (this.phase == Phase.CLOSED) {
        return;
      }
      this.phase = Phase.CLOSED;
      try {
        this.origin.afterSuite(this.context);
      } catch (RuntimeException failure) {
        this.failures.forEach(failure::addSuppressed);
        this.failures.add(failure);
        throw failure;
      }
    }
  }

  private enum Phase {
    READY,
    STARTED,
    CLOSED
  }
}
