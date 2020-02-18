package test.duplicate;

import org.testng.annotations.Test;

/**
 * Created by EasonYi
 */
public class FooTest {
  @Test
  public void foo1() {
    System.out.println("foo1_" + Id.ID.getAndIncrement());
  }

  @Test
  public void foo2() {
    System.out.println("foo2_" + Id.ID.getAndIncrement());
  }

  @Test
  public void foo3() {
    System.out.println("foo3_" + Id.ID.getAndIncrement());
  }

  @Test
  public void foo4() {
    int id = Id.ID.getAndIncrement();
    System.out.println("begin foo4_" + id);
    TestUtil.sleepSomeMilliSeconds(1000);
    System.out.println("end foo4_" + id);
  }
}
