package test.testng173;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

public class TestNG173Test extends SimpleBaseTest {

  @Test
  public void orderShouldBePreservedInMethodsWithSameNameAndInDifferentClasses() {
    TestNG tng = create();
    XmlSuite s = createXmlSuite("PreserveOrder");
    XmlTest t = new XmlTest(s);

    t.getXmlClasses().add(new XmlClass("test.testng173.ClassA"));
    t.getXmlClasses().add(new XmlClass("test.testng173.ClassB"));

    t.setPreserveOrder(true);

    tng.setXmlSuites(Arrays.asList(s));

    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener((ITestNGListener) listener);
    tng.run();

    // bug
    // verifyPassedTests(tla, "test1", "test2", "testX", "test1", "test2");

    // Proposed fix
    assertThat(listener.getSucceedMethodNames())
        .containsExactly("test1", "test2", "testX", "test2", "test1");
  }

  @Test
  public void
      orderShouldBePreservedInMethodsWithSameNameAndInDifferentClassesAndDifferentPackage() {
    TestNG tng = create();
    XmlSuite s = createXmlSuite("PreserveOrder");
    XmlTest t = new XmlTest(s);

    t.getXmlClasses().add(new XmlClass("test.testng173.ClassA"));
    t.getXmlClasses().add(new XmlClass("test.testng173.anotherpackage.ClassC"));

    t.setPreserveOrder(true);

    tng.setXmlSuites(Arrays.asList(s));

    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener((ITestNGListener) listener);
    tng.run();

    // bug
    // verifyPassedTests(tla, "test1", "test2", "testX", "test1", "test2");

    assertThat(listener.getSucceedMethodNames())
        .containsExactly("test1", "test2", "testX", "test2", "test1");
  }
}
