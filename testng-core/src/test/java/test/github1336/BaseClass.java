package test.github1336;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;

public class BaseClass {
  private FakeDriver driver;

  @BeforeClass
  public void beforeClass() {
    driver = new FakeDriver();
  }

  private FakeDriver getDriver() {
    return driver;
  }

  public static class FakeDriver {
    private String url;

    public void get(String url) {
      this.url = url;
    }

    String getCurrentUrl() {
      try {
        TimeUnit.SECONDS.sleep(new Random().nextInt(10));
        return url;
      } catch (InterruptedException e) {
        return url;
      }
    }
  }

  void runTest(String url) {
    getDriver().get(url);
    Assert.assertEquals(getDriver().getCurrentUrl(), url);
  }
}
