package test.factory.issue1924;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  @Test
  public void ensureTestClassInstantiationWorksWhenFactoryMethodAndCustomConstructorPresent() {
    TestNG testng = create(TestclassSample.class);
    testng.run();
    List<String> expected = Arrays.asList("1", "2");
    assertThat(TestclassSample.logs).containsExactlyInAnyOrderElementsOf(expected);
  }
}
