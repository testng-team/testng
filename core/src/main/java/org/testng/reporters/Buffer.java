package org.testng.reporters;

public class Buffer {
  public static IBuffer create() {
    return new FileStringBuffer();
  }
}
