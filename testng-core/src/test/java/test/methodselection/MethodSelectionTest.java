package test.methodselection;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

public class MethodSelectionTest extends SimpleBaseTest {

  @DataProvider(name = "dp")
  public static Object[][] getData() {
    return new Object[][] {
      // Method name
      {"$", "$"},
      {"another_test", "another_test"},
      {"another$_test", "another$_test"},
      {"another$_test$", "another$_test$"},
      // Regex
      {"another\\$_test\\$$", "another$_test$"},
      {"\\$.*", new String[] {"$", "$another$_test$"}},
      {"\\$.*$", new String[] {"$", "$another$_test$"}}
    };
  }

  @Test(dataProvider = "dp")
  public void testDollarMethodSelection(String methodName, String... expected) {
    XmlSuite xmlSuite = createXmlSuite("Dollar method Suite");
    XmlTest xmlTest = createXmlTest(xmlSuite, "Dollar method Test");
    XmlClass xmlClass = createXmlClass(xmlTest, DollarMethodSample.class);
    createXmlInclude(xmlClass, methodName);
    InvokedMethodNameListener listener = run(xmlSuite);
    assertThat(listener.getInvokedMethodNames()).containsExactly(expected);
  }
}
