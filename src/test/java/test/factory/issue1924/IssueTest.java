package test.factory.issue1924;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  @Test
  public void ensureTestClassInstantiationWorksWhenFactoryMethodAndCustomConstructorPresent() {
    TestNG testng = create(TestclassSample.class);
    testng.setVerbose(2);
    testng.run();
    List<String> expected = Arrays.asList("1", "2");
    assertThat(TestclassSample.logs).containsExactlyInAnyOrderElementsOf(expected);
  }

}
