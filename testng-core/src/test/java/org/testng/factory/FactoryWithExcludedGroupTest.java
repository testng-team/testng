package org.testng.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.factory.samples.ExcludedFactory.EXCLUDED_GROUP;

import java.util.Collections;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.factory.samples.ExcludedFactory;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

public class FactoryWithExcludedGroupTest extends SimpleBaseTest {

  @Test
  public void testFactoryExecutionWhenNoIncludedTests() {
    XmlSuite suite = createXmlSuite("Suite");
    XmlTest test = createXmlTest(suite, "Test", ExcludedFactory.class);
    test.setExcludedGroups(Collections.singletonList(EXCLUDED_GROUP));
    TestNG tng = create(suite);

    tng.run();

    assertThat(ExcludedFactory.factoryRan).isFalse();
  }
}
