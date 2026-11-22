package org.testng.reporters;

import java.io.Writer;

public interface IBuffer {
  IBuffer append(CharSequence string);

  void toWriter(Writer fw);

  /**
   * Returns whether {@code toString()} can supply the complete content from memory.
   *
   * <p>Return {@code false} if this is unknown. The default keeps existing implementations on the
   * streaming path.
   *
   * @return {@code true} if the complete content is available in memory
   */
  default boolean isInMemory() {
    return false;
  }
}
