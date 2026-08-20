package org.testng.reporters.snapshot;

/**
 * A parameter a test can change under a reporter's feet: mutable, and nothing else -- in particular
 * not {@link Cloneable}, so the historical clone-if-{@code Cloneable} rule cannot help.
 */
public final class MutableParameter {

  private String value;

  MutableParameter(String value) {
    this.value = value;
  }

  void set(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value;
  }
}
