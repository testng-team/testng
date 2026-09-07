package test.failedreporter.issue2940;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.concurrent.atomic.AtomicInteger;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DependsOnGroupsDataProviderSample {
  public static final AtomicInteger remainingFailures = new AtomicInteger();

  private String test1result;
  private String test2result;

  @Test(groups = "group")
  public void test1() {
    test1result = "something";
  }

  @Test(groups = "group")
  public void test2() {
    test2result = "something also";
    if (remainingFailures.getAndDecrement() > 0) {
      fail();
    }
  }

  @DataProvider
  public Object[][] dpResults() {
    return new Object[][] {{test1result}, {test2result}};
  }

  @Test(dependsOnGroups = "group", dataProvider = "dpResults")
  public void test3(String result) {
    assertThat(result).isNotNull();
  }
}
