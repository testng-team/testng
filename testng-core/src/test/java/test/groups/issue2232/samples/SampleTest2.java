package test.groups.issue2232.samples;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@Test(groups = {"Group1", "Group2", "Group3"})
public class SampleTest2 {

  private int variable = 0;

  @BeforeClass
  public void setUp() throws Exception {
    variable += 1;
    if (variable == 4) {
      throw new Exception("Exception!");
    }
  }

  @AfterMethod
  public void tearDown() {
    variable += 1;
  }

  @DataProvider(name = "testData2")
  public Object[][] testData2() {
    return new Object[][] {
      {"Test3"}, {"Test4"},
    };
  }

  @Test(dataProvider = "testData2")
  public void test2(String test) {
    variable += 1;
  }
}
