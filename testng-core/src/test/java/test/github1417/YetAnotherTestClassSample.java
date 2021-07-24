package test.github1417;

import java.util.ArrayList;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class YetAnotherTestClassSample {
  private static YetAnotherTestClassSample instance;

  private List<String> browsers = new ArrayList<>();

  public YetAnotherTestClassSample() {
    setInstance(this);
  }

  private void setInstance(YetAnotherTestClassSample obj) {
    instance = obj;
  }

  public static YetAnotherTestClassSample getInstance() {
    return instance;
  }

  @Parameters({"browsername"})
  @BeforeClass
  public void beforeClass(String browser) {
    browsers.add(browser);
  }

  @Test
  public void testMethod() {
    Assert.assertFalse(browsers.isEmpty());
  }

  @Parameters({"browsername"})
  @AfterClass
  public void afterClass(String browser) {
    browsers.add(browser);
  }

  List<String> getBrowsers() {
    return browsers;
  }
}
