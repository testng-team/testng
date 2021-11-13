package test.listeners.github1296;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;

public class GitHub1296Test extends SimpleBaseTest {

  @Test(description = "https://github.com/cbeust/testng/issues/1296")
  public void test_number_of_call_of_configuration_listener() {
    MyConfigurationListener.CALLS.clear();
    XmlSuite suite = createXmlSuite("Tests");
    createXmlTest(suite, "Test version", MyTest.class);
    createXmlTest(suite, "Test version 2", MyTest.class);
    createXmlTest(suite, "Test version 3", MyTest.class);
    TestNG tng = create(suite);

    tng.run();

    assertThat(MyConfigurationListener.CALLS)
        .hasSize(3)
        .containsOnly(
            entry("Test version", 2), entry("Test version 2", 2), entry("Test version 3", 2));
  }
}
