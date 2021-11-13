package test.junit4;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class BeforeClassJUnit4Sample {

  @BeforeClass
  public static void before() {
    throw new IllegalArgumentException("before failed");
  }

  @AfterClass
  public static void after() {
    // throw new IllegalArgumentException("after failed");
  }

  @Test
  public void myTest() {
    System.out.println("yay!");
  }
}
