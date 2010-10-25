package test.tmp;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class B extends C {
  public static final String G = "group";


  @BeforeMethod
  public void bm() {
    System.out.println("B.bm");
  }

  @Test
  public void btest1() {
    System.out.println("B.btest1");
  }
}

