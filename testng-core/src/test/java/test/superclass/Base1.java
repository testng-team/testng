package test.superclass;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Base1 {
  @BeforeClass
  public void bc() {}

  @BeforeMethod
  public void bm() {}

  @Test
  public void tbase() {}
}
