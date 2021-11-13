package test.parameters;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

public class ShadowTest extends SimpleBaseTest {

  @Test
  public void parametersShouldNotBeShadowed() {
    XmlSuite suite = createXmlSuite("suite");
    XmlTest test = createXmlTest(suite, "test");

    XmlClass class1 = createXmlClass(test, Shadow1Sample.class);
    class1.getLocalParameters().put("a", "First");
    XmlInclude include1 = createXmlInclude(class1, "test1");

    XmlClass class2 = createXmlClass(test, Shadow2Sample.class);
    class2.getLocalParameters().put("a", "Second");
    XmlInclude include2 = createXmlInclude(class2, "test2");

    TestNG tng = create(suite);

    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener((ITestNGListener) listener);

    tng.run();

    assertThat(listener.getSucceedMethodNames()).containsExactly("test1(First)", "test2(Second)");
  }
}
