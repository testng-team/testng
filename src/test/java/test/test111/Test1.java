package test.test111;

import junit.framework.Assert;
import org.testng.annotations.Test;

public class Test1 extends AbstractTest {

  @Test
  public void test() {
    Assert.assertEquals(0, AbstractTest.R);
  }
}