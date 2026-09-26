package org.testng.configuration.issue2426;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.configuration.samples.issue2426.MyMethodListener;
import org.testng.configuration.samples.issue2426.SampleTestCase;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  @Test(description = "GITHUB-2426")
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
