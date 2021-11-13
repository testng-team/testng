package org.testng.annotations;

/** Parent interface for annotations that can receive parameters. */
public interface IParameterizable extends IAnnotation {
  /**
   * Whether this annotation is enabled.
   *
   * @return true if enabled
   */
  boolean getEnabled();

  void setEnabled(boolean enabled);
}
