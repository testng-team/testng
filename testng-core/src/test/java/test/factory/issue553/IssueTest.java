package test.factory.issue553;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.TestNGException;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  @Test(
      description = "GITHUB-553",
      expectedExceptions = TestNGException.class,
      expectedExceptionsMessageRegExp =
          "\\nFound a default constructor and also a Factory method when working with .*")
  public void testMethod() {
    XmlTest xmltest = createXmlTest("suite", "test", Concrete.class);
    TestNG testng = create(xmltest.getSuite());
    TestListenerAdapter listener = new TestListenerAdapter();
    testng.addListener(listener);
    testng.run();
  }
}
