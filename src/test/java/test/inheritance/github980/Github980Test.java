package test.inheritance.github980;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

public class Github980Test extends SimpleBaseTest {

  @Test
  public void testToEnsureDuplicateMethodsAreNotExecuted() {
    XmlSuite suite = createXmlSuite("980_suite");
    XmlTest xmltest = createXmlTest(suite, "980_test");
    XmlClass parentClass = createXmlClass(xmltest, ParentClassSample.class);
    createXmlInclude(parentClass, "a");
    createXmlInclude(parentClass, "b");
    XmlClass childClass = createXmlClass(xmltest, ChildClassSample.class);
    createXmlInclude(childClass, "c");
    createXmlInclude(childClass, "d");
    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    TestNG tng = create(suite);
    tng.addListener(listener);
    tng.run();
    List<String> parentClassMethods = listener.getMethodsForTestClass(ParentClassSample.class);
    assertThat(parentClassMethods).containsExactly("a", "b");
    List<String> childClassMethods = listener.getMethodsForTestClass(ChildClassSample.class);
    assertThat(childClassMethods).containsExactly("c", "d");
  }
}
