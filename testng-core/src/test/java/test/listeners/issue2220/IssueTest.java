package test.listeners.issue2220;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  @Test
  public void ensureAnnotatedListenersAreWiredInOnlyOnce() {
    XmlSuite xmlsuite = createXmlSuite("my_suite");
    createXmlTest(xmlsuite, "test_1", TestClass01.class);
    createXmlTest(xmlsuite, "test_2", TestClass02.class);
    TestNG testng = create(xmlsuite);
    testng.run();
    List<String> expected =
        Arrays.asList(
            "started_<test>_test_1",
            "started_test_method_test.listeners.issue2220.TestClass01.test01",
            "started_test_method_test.listeners.issue2220.TestClass01.test02",
            "started_test_method_test.listeners.issue2220.TestClass01.test03",
            "finished_<test>_test_1",
            "started_<test>_test_2",
            "started_test_method_test.listeners.issue2220.TestClass02.test01",
            "started_test_method_test.listeners.issue2220.TestClass02.test02",
            "started_test_method_test.listeners.issue2220.TestClass02.test03",
            "finished_<test>_test_2");
    assertThat(Listener1.logs).containsExactlyElementsOf(expected);
  }
}
