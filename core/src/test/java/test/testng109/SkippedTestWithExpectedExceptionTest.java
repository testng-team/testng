package test.testng109;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * This class/interface
 */
public class SkippedTestWithExpectedExceptionTest {
  @BeforeClass
  public void setup() {
    throw new RuntimeException("test-exception");
  }

  @Test
  public void test1()
  {
//   empty
  }

  @Test(expectedExceptions={OutOfMemoryError.class})
  public void test2()
  {
//  empty
  }
}
