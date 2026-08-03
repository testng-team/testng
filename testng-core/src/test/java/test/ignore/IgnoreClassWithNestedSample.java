package test.ignore;

import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

@Ignore
public class IgnoreClassWithNestedSample {

  @Test
  public void outerTest() {}

  public static class Nested {

    @Test
    public void nestedTest() {}
  }
}
