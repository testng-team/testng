package test;

import org.testng.annotations.Test;

public class NestedStaticSampleTest {

  @Test
  public void f() {}

  public static class Nested {
    @Test
    public void nested() {}
  }
}
