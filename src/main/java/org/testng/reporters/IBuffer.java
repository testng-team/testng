package org.testng.reporters;

import java.io.Writer;

public interface IBuffer {
  IBuffer append(CharSequence string);
  void toWriter(Writer fw);
}
