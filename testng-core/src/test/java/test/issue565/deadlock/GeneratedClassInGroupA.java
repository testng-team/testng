package test.issue565.deadlock;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class GeneratedClassInGroupA {

  private final int id;

  public GeneratedClassInGroupA(int id) {
    this.id = id;
  }

  @BeforeClass(groups = "A")
  public void init() {}

  @Test(groups = "A")
  public void test1() {}

  @Test(groups = "A", dependsOnMethods = "test4")
  public void test2() {}

  @Test(groups = "A")
  public void test3() {}

  @Test(groups = "A", dependsOnMethods = "test3")
  public void test4() {}

  @Override
  public String toString() {
    return "GeneratedClassInGroupA{" + id + '}';
  }
}
