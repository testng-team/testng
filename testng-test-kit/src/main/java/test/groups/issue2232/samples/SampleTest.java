package test.groups.issue2232.samples;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@Test(groups = {"Group1", "Group2", "Group3"})
public class SampleTest {

  @BeforeClass
  public void setUp() {}

  @AfterMethod
  public void tearDown() {}

  @DataProvider(name = "testData1")
  public Object[][] testData1() {
    return new Object[][] {
      {"Test1"}, {"Test2"},
    };
  }

  @Test(dataProvider = "testData1")
  public void test1(String test) {}
}
