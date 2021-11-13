package test.groups.issue2232.samples;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@Test(groups = {"Group1", "Group2", "Group3"})
public class SampleTest {

  private int variable = 0;

  @BeforeClass
  public void setUp() {
    variable += 1;
  }

  @AfterMethod
  public void tearDown() {
    variable += 1;
  }

  @DataProvider(name = "testData1")
  public Object[][] testData1() {
    return new Object[][] {
      {"Test1"}, {"Test2"},
    };
  }

  @Test(dataProvider = "testData1")
  public void test1(String test) {
    variable += 1;
  }
}
