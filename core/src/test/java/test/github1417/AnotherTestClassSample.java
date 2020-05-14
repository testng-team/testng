package test.github1417;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class AnotherTestClassSample {

  private static AnotherTestClassSample instance;
  private String browsername;

  public AnotherTestClassSample() {
    setInstance(this);
  }

  private void setInstance(AnotherTestClassSample obj) {
    instance = obj;
  }

  public static AnotherTestClassSample getInstance() {
    return instance;
  }

  String getBrowsername() {
    return browsername;
  }

  @Parameters({"browsername"})
  @BeforeClass
  public void beforeClass(String browsername) {
    this.browsername = browsername;
  }

  @Parameters({"browsername"})
  @AfterClass
  public void afterClass(String browsername) {
    this.browsername = browsername;
  }

  @Test
  public void testMethod() {
    Assert.assertEquals("chrome", browsername);
  }
}
