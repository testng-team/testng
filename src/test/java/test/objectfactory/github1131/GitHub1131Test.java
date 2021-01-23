package test.objectfactory.github1131;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;

public class GitHub1131Test extends SimpleBaseTest {

  @DataProvider
  public static Object[][] dp() {
    return new Object[][]{
        new Object[]{true},
        new Object[]{false}
    };
  }

  private void testFactory(boolean onSuite, Class<?> sample) {
    MyObjectFactory.allParams.clear();

    XmlSuite suite = createXmlSuite("Test IObjectFactory2", "TmpTest", sample);
    TestNG tng = create(suite);

    if (onSuite) {
      suite.setObjectFactory(new MyObjectFactory());
    } else {
      tng.setObjectFactory(MyObjectFactory.class);
    }

    tng.run();
  }

  @Test(dataProvider = "dp")
  public void factoryWithEmptyConstructorShouldWork(boolean bool) {
    testFactory(bool, EmptyConstructorSample.class);
    assertThat(MyObjectFactory.allParams).isEmpty();
  }

  @Test(dataProvider = "dp")
  public void factoryWithIntConstructorShouldWork(boolean bool) {
    testFactory(bool, IntConstructorSample.class);
    assertThat(MyObjectFactory.allParams).containsExactly(new Object[]{1}, new Object[]{2});
  }

  @Test(dataProvider = "dp")
  public void factoryWithStringConstructorShouldWork(boolean bool) {
    testFactory(bool, StringConstructorSample.class);
    assertThat(MyObjectFactory.allParams).containsExactly(new Object[]{"foo"}, new Object[]{"bar"});
  }
}
