package test.dataprovider.issue2255;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  @Test(description = "GITHUB-2255")
  public void runTest() {
    TestNG testNG = create(SampleTestCase.class);
    testNG.run();
    assertThat(SampleTestCase.data).containsExactly(100, 200);
  }
}
