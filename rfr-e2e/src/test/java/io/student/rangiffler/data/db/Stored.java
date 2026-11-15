package io.student.rangiffler.data.db;

public interface Stored {
  void create();

  void delete();

  boolean exists();
}
