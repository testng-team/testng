package test.tmp;

import org.testng.ITestContext;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class B {

  @Test
  public void testEchoB(ITestContext c) {
    System.out.println("B Context:" + c.getSuite().getAttribute("test"));
  }

}
