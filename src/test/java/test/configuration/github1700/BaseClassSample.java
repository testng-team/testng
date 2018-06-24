package test.configuration.github1700;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.collections.Lists;

import java.lang.reflect.Method;
import java.util.List;

public class BaseClassSample {

  public static List<String> messages = Lists.newArrayList();

  @BeforeMethod(alwaysRun = true)
  public void setUp(Method method) {
    if (method.getName().endsWith("test1")) {
      Assert.assertEquals(2, 1);
    }
    messages.add(getClass().getCanonicalName() + ".setup()");
  }
}
