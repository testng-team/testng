package test.testng56;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * This class/interface
 */
public class ParallelTest {
  @BeforeClass
  public void setup() {
    System.out.println(Thread.currentThread().getId() + ":setup");
  }

  @AfterClass
  public void teardown() {
    System.out.println(Thread.currentThread().getId() + ":teardown");
  }

  @Test
  public void test1() {
    System.out.println(Thread.currentThread().getId() + ":test1");
  }

  @Test(dependsOnMethods = {"test1"})
  public void test2() {
    System.out.println(Thread.currentThread().getId() + ":test2");
  }

  @Test
  public void test3() {
    System.out.println(Thread.currentThread().getId() + ":test3");
  }

  @Test(dependsOnMethods = {"test3"})
  public void test4() {
    System.out.println(Thread.currentThread().getId() + ":test4");
  }

  @Test
  public void test5() {
    System.out.println(Thread.currentThread().getId() + ":test5");
  }
}
