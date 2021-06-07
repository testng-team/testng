package test.parameters;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

public class ParameterOverrideTest extends SimpleBaseTest {

  enum Status {
    PASS_TEST,
    PASS_CLASS,
    PASS_INCLUDE
  }

  @DataProvider
  public static Object[][] dp() {
    return new Object[][] {
      new Object[] {"testOverrideSuite", Status.PASS_TEST},
      new Object[] {"classOverrideSuite", Status.PASS_CLASS},
      new Object[] {"includeOverrideClass", Status.PASS_INCLUDE},
    };
  }

  @Test(dataProvider = "dp")
  public void testOverrideParameter(String name, Status status) {
    XmlSuite suite = createXmlSuite("suite");
    suite.getParameters().put("a", "Incorrect");
    suite.getParameters().put("InheritedFromSuite", "InheritedFromSuite");

    XmlTest test = createXmlTest(suite, "test");
    test.getLocalParameters().put("InheritedFromTest", "InheritedFromTest");

    XmlClass clazz = createXmlClass(test, Override1Sample.class);
    clazz.getLocalParameters().put("InheritedFromClass", "InheritedFromClass");

    XmlInclude includeF = createXmlInclude(clazz, "f");
    XmlInclude includeG = createXmlInclude(clazz, "g");

    switch (status) {
      case PASS_TEST:
        test.getLocalParameters().put("a", "Correct");
        break;
      case PASS_CLASS:
        clazz.getLocalParameters().put("a", "Correct");
        break;
      case PASS_INCLUDE:
        includeF.getLocalParameters().put("a", "Correct");
        break;
    }

    TestNG tng = create(suite);

    InvokedMethodNameListener tla = new InvokedMethodNameListener();
    tng.addListener((ITestNGListener) tla);

    tng.run();

    assertThat(tla.getSucceedMethodNames())
        .containsExactly(
            "f(Correct)", "g(InheritedFromSuite,InheritedFromTest,InheritedFromClass)");
  }
}
