package test.tmp;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.util.List;

public class C extends B {
  @BeforeClass
  public void beforeC() {
    System.out.println("Before C");
  }

  @Test
  public void f() {}
}
