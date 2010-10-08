package test.dependent;

import org.testng.annotations.Test;

@Test(groups = { "checkin" })
public class MultiGroup1SampleTest {

  @Test(groups = {"a"})
  public void testA() {

  }

  public void test1() throws Exception {
    throw new Exception("fail");
  }
}
