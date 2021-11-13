package test.retryAnalyzer.github1519;

import java.util.List;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

public class TestClassSample {
  static boolean retry = false;
  public static List<String> messages = Lists.newArrayList();

  @Test(retryAnalyzer = MyAnalyzer.class)
  public void testMethod() {
    Assert.assertTrue(retry);
  }
}
