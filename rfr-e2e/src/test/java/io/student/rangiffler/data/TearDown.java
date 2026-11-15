package io.student.rangiffler.data;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class TearDown implements AutoCloseable {
  private final List<Runnable> steps;

  public TearDown() {
    this(List.of());
  }

  private TearDown(List<Runnable> steps) {
    this.steps = List.copyOf(steps);
  }

  public TearDown with(Runnable step) {
    List<Runnable> next = new ArrayList<>(steps);
    next.add(step);
    return new TearDown(next);
  }

  public <T> TearDown withEach(Iterable<T> items, Consumer<T> action) {
    List<Runnable> next = new ArrayList<>(steps);
    for (T item : items) {
      next.add(() -> action.accept(item));
    }
    return new TearDown(next);
  }

  @Override
  public void close() {
    List<RuntimeException> failures = new ArrayList<>();
    for (Runnable step : steps) {
      try {
        step.run();
      } catch (RuntimeException failure) {
        failures.add(failure);
      }
    }
    if (failures.isEmpty()) {
      return;
    }
    RuntimeException first = failures.getFirst();
    failures.stream().skip(1).forEach(first::addSuppressed);
    throw first;
  }
}
