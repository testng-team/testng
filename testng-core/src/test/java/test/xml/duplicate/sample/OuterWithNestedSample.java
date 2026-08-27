package test.xml.duplicate.sample;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class OuterWithNestedSample {

  @Test
  @Parameters("id")
  public void outer(String id) {}

  public static class Nested {
    @Test
    @Parameters("id")
    public void nested(String id) {}
  }
}
