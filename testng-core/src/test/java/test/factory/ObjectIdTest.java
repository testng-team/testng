package test.factory;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.assertj.core.api.SoftAssertions;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;
import test.factory.issue3079.FactoryTestCase;
import test.factory.issue3079.SampleTestCase;

public class ObjectIdTest extends SimpleBaseTest {

  @Test(description = "GITHUB-3079")
  public void ensureOnlyOneObjectIdExistsForNormalTestClass() {
    TestNG testng = create(SampleTestCase.class);
    testng.setParallel(XmlSuite.ParallelMode.METHODS);
    testng.run();
    Map<UUID, Set<Object>> objectMap = SampleTestCase.objectMap;
    assertThat(objectMap.keySet()).hasSize(1);
    SoftAssertions assertions = new SoftAssertions();
    objectMap.forEach((key, value) -> assertions.assertThat(value).hasSize(1));
    assertions.assertAll();
  }

  @Test(description = "GITHUB-3079")
  public void ensureOnlyOneObjectIdExistsForFactoryPoweredTestClass() {
    TestNG testng = create(FactoryTestCase.class);
    testng.setParallel(XmlSuite.ParallelMode.INSTANCES);
    testng.run();
    Map<UUID, Set<Object>> objectMap = FactoryTestCase.objectMap;
    assertThat(objectMap.keySet()).hasSize(100);
    SoftAssertions assertions = new SoftAssertions();
    objectMap.forEach((key, value) -> assertions.assertThat(value).hasSize(1));
    assertions.assertAll();
  }
}
