package org.testng.reporters;

import java.io.Writer;

public interface IBuffer {
  IBuffer append(CharSequence string);

  void toWriter(Writer fw);

  /**
   * Returns the character count when the complete content is available in memory.
   *
   * <p>This method does not read spilled content or create a string. Return {@code -1} if the size
   * is unknown. The default keeps existing implementations on the streaming path.
   *
   * @return the complete in-memory character count, or {@code -1} if unavailable
   */
  default int getInMemorySize() {
    return -1;
  }
}
