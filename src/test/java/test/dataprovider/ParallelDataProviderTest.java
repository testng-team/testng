package test.dataprovider;

import org.testng.ITestContext;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

/**
 * Data providers were not working properly with parallel=true
 * @author cbeust
 */
public class ParallelDataProviderTest extends SimpleBaseTest {
//  protected static Logger logger = Logger
//    .getLogger(SampleMessageLoaderTest2.class);
//  // This method will provide data to any test method that declares that its
  // Data Provider
  // is named "test1"
  @DataProvider(name = "test1", parallel = true)
  public Object[][] createData1() {
   return new Object[][] {
     { "Cedric", 36},
     { "Anne", 37},
     { "A", 36},
     { "B", 37}
   };
  }
  // This test method declares that its data should be supplied by the Data
  // Provider
  // named "test1"
  @Test(dataProvider = "test1", threadPoolSize = 5)
  public void verifyData1(ITestContext testContext, String n1, Integer n2) {
  }

  @Test
  public void shouldNotThrowConcurrentModificationException() {
    TestNG tng = create(ParallelDataProvider2Test.class);
    tng.run();
  }
}
