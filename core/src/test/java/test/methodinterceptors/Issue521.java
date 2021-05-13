package test.methodinterceptors;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class Issue521 {

  @BeforeClass
  public void beforeClass() {}

  @Test
  public void test1() {}

  @Test
  public void test2() {}

}
