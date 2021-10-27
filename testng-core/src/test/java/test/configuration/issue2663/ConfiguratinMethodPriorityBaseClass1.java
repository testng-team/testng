package test.configuration.issue2663;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class ConfiguratinMethodPriorityBaseClass1 {
  @BeforeSuite(
      groups = {"g1"},
      priority = 2)
  public void s3() {
    System.out.println("s1");
  }

  @BeforeClass(
      groups = {"g1"},
      priority = 2)
  public void c3() {
    System.out.println("c1");
  }

  @BeforeTest(
      groups = {"g1"},
      priority = 2)
  public void t3() {
    System.out.println("t1");
  }

  @BeforeMethod(
      groups = {"g1"},
      priority = 2)
  public void m3() {
    System.out.println("m1");
  }

  @Test(groups = {"g1"})
  public void test3() {
    System.out.println("test1");
  }
}
