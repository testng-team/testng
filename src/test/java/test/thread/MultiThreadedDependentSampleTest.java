package test.thread;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.util.List;

public class MultiThreadedDependentSampleTest {
  public static List<String> m_methods = Lists.newArrayList();

  @BeforeClass
  public void bc() {
//    log("beforeClass");
  }

  @AfterClass
  public void ac() {
//    log("afterClass");
  }

  @BeforeMethod
  public void bm() {
//    log("beforeMethod");
  }

  @AfterMethod
  public void am() {
//    log("afterMethod");
  }

  @Test(groups = "1")
  public void a1() {
    logThread();
    log("a1");
  }

  @Test(groups = "1")
  public void a2() {
    logThread();
    log("a2");
  }

  @Test(groups = "1")
  public void a3() {
    logThread();
    log("a3");
  }

  @Test(groups = "2", dependsOnGroups = "1")
  public void b1() {
    logThread();
    log("b1");
  }

  @Test(groups = "2", dependsOnGroups = "1")
  public void b2() {
    logThread();
    log("b2");
  }

  @Test(groups = "2", dependsOnGroups = "1")
  public void b3() {
    logThread();
    log("b3");
  }

  @Test(groups = "2", dependsOnGroups = "1")
  public void b4() {
    logThread();
    log("b4");
  }

  @Test(groups = "2", dependsOnGroups = "1")
  public void b5() {
    logThread();
    log("b5");
  }

  @Test(dependsOnGroups = "2")
  public void c1() {
    logThread();
    log("c1");
  }

  @Test(dependsOnGroups = { "1" })
  public void d() {
    logThread();
    log("d");
  }

  @Test
  public void x() {
    log("x");
  }

  @Test
  public void y() {
    log("y");
  }

  @Test
  public void z() {
    log("z");
  }

  @Test
  public void t() {
    log("t");
  }

  //  @Test(groups = "mytest", dependsOnMethods = "g")
//  public void f() {
//  }
//
//
//  @AfterMethod
//  public void after() {
//  }

  private void logThread() {
    long id = Thread.currentThread().getId();
    Helper.getMap(getClass().getName()).put(id, id);
  }

  private void log(String string) {
    synchronized(m_methods) {
      m_methods.add(string);
    }
  }

}
