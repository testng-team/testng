package test.nested2;

import org.testng.annotations.Test;

public class TmpA {
  public static class NestedAWithTest {
    @Test
    public void nestedA() {}
  }

  public static class NestedAWithoutTest {
    public NestedAWithoutTest() {
      throw new RuntimeException("TestNG should not instantiate me");
    }

    public void nestedA() {}
  }

  @Test
  public static class DummyBase {}

  public static class NestedAWithInheritedTest extends DummyBase {
    public void nestedWithInheritedTest() {}
  }

}