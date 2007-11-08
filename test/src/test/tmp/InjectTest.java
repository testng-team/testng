package test.tmp;

import org.testng.ITestContext;
import org.testng.annotations.Test;

public class InjectTest {

  @Test
  public void t(ITestContext tc) {
    System.out.println("ITestContext:" + tc);
  }
}
