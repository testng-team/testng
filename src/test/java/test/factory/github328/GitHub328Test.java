package test.factory.github328;

import static test.factory.github328.ExcludedFactory.EXCLUDED_GROUP;

import java.util.Collections;
import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

public class GitHub328Test extends SimpleBaseTest {

  @Test
  public void testFactoryExecutionWhenNoIncludedTests() {
    XmlSuite suite = createXmlSuite("Suite");
    XmlTest test = createXmlTest(suite, "Test", ExcludedFactory.class);
    test.setExcludedGroups(Collections.singletonList(EXCLUDED_GROUP));
    TestNG tng = create(suite);

    tng.run();

    Assert.assertFalse(ExcludedFactory.factoryRan);
  }
}
