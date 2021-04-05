package test.factory.issue1924;

import java.util.List;
import org.testng.Reporter;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

public class TestclassSample {
  public static List<String> logs = Lists.newArrayList();

  private int i;

  public TestclassSample(int i) {
    this.i = i;
  }

  @Factory
  public static Object[] produce() {
    return new Object[]{
        new TestclassSample(1),
        new TestclassSample(2)
    };
  }

  @Test
  public void testMethod() {
    logs.add(Integer.toString(this.i));
  }
}
