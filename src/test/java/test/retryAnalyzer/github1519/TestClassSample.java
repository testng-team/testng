package test.retryAnalyzer.github1519;

import java.util.List;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

public class TestClassSample {

  public static List<String> messages = Lists.newArrayList();
  static boolean retry = false;

  @Test(retryAnalyzer = MyAnalyzer.class)
  public void testMethod() {
    Assert.assertTrue(retry);
  }
}