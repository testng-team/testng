package test.tmp;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class A {
  @Test(groups = { "database", "foo" })
  public void test1() { }
  
}
