package test.factory.issue1041;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  @Test
  public void testFactoryAnnotatedConstructor() {
    TestNG testng = create(FactoryAnnotatedConstructorExample.class);
    ResultExtractor extractor = new ResultExtractor();
    testng.addListener(extractor);
    testng.run();
    List<Object[]> expected = new ArrayList<>();
    expected.add(new Object[] {1});
    expected.add(new Object[] {2});
    List<Object[]> actual =
        FactoryAnnotatedConstructorExample.objects.stream()
            .map(extractor::getData)
            .collect(Collectors.toList());
    assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
  }
}
