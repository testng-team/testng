package test.morten;

import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class SampleTest {
  public static class SampleTestTestFactory {
    @Factory
    public Object[] createInstances() {
      return new SampleTest[] {
        new SampleTest(1, 0.1f), new SampleTest(10, 0.5f),
      };
    }
  }

  public SampleTest(int capacity, float ignored) {
    System.out.println("CREATING TEST WITH " + capacity);
  }

  @Test
  public void testPut() {}
}
