package test.configuration.issue2426;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  @Test
  public void testIfConfigMethodsHaveAccessToFactoryParams() {
    TestNG testng = create(SampleTestCase.class);
    MyMethodListener listener = new MyMethodListener();
    testng.addListener(listener);
    testng.run();
    Map<Class<?>, Object[]> data = listener.getContents();
    assertThat(data).hasSize(8);
    data.values().forEach(each -> assertThat(each).hasSize(2));
  }
}
