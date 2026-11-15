package io.student.rangiffler.data;

import io.qameta.allure.Step;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public final class StaticUserFixture implements UserFixture {
  private static final long WAIT_SECONDS = 10L;
  private static final BlockingQueue<TestUser> USERS = new LinkedBlockingQueue<>(List.of(
      new TestUser("static-user-1", "static-password-1"),
      new TestUser("static-user-2", "static-password-2"),
      new TestUser("static-user-3", "static-password-3")
  ));
  private TestUser user;

  public StaticUserFixture() {
    this.user = null;
  }

  @Override
  public TestUser user() {
    if (user == null) {
      throw new IllegalStateException("Static user is not taken");
    }
    return user;
  }

  @Step("Take static user from queue")
  @Override
  public void create() {
    if (user != null) {
      throw new IllegalStateException("Static user is already taken");
    }
    user = next();
  }

  @Step("Return static user to queue")
  @Override
  public void close() {
    if (user == null) {
      return;
    }
    USERS.add(user);
    user = null;
  }

  private TestUser next() {
    try {
      TestUser next = USERS.poll(WAIT_SECONDS, TimeUnit.SECONDS);
      if (next == null) {
        throw new IllegalStateException("No static user became available within " + WAIT_SECONDS + " seconds");
      }
      return next;
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Interrupted while waiting for a static user", exception);
    }
  }
}
