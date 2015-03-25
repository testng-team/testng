package test.issue565.deadlock;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class GeneratedClassInGroupA {
  private int id;

  public GeneratedClassInGroupA(int id) {
    this.id = id;
  }

  @BeforeClass(groups = "A")
  public void init() {
    System.out.println("GeneratedClassInGroupA.init " + id);
  }

  @Test(groups = "A")
  public void test1() {
    System.out.println("GeneratedClassInGroupA.test1");
  }

  @Test(groups = "A", dependsOnMethods = "test4")
  public void test2() {
    System.out.println("GeneratedClassInGroupA.test2");
  }

  @Test(groups = "A")
  public void test3() {
    System.out.println("GeneratedClassInGroupA.test3");
  }

  @Test(groups = "A", dependsOnMethods = "test3")
  public void test4() {
    System.out.println("GeneratedClassInGroupA.test4");
  }

  @Override
  public String toString() {
    return "GeneratedClassInGroupA{" + id + '}';
  }
}
