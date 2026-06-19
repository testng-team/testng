package test.name;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.ITest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

// From https://stackoverflow.com/q/33404335/4234729
public class ITestSample implements ITest {

  public ThreadLocal<String> testName = new ThreadLocal<>();

  @DataProvider(name = "dp", parallel = true)
  public Object[][] getTests() {
    return new Object[][] {
      new Object[] {"test1"},
      new Object[] {"test2"},
      new Object[] {"test3"},
      new Object[] {"test4"},
      new Object[] {"test5"}
    };
  }

  @Test(dataProvider = "dp")
  public void run(String testName) {
    assertThat(this.testName.get()).isEqualTo(testName);
  }

  @BeforeMethod
  public void init(Object[] testArgs) {
    testName.set((String) testArgs[0]);
  }

  @AfterMethod
  public void tearDown(Object[] testArgs) {
    assertThat(this.testName.get()).isEqualTo((String) testArgs[0]);
  }

  @Override
  public String getTestName() {
    return testName.get();
  }
}
