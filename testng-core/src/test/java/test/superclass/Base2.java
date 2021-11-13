package test.superclass;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test
public class Base2 {
  @BeforeClass
  public void bc() {}

  @BeforeMethod
  public void bm() {}

  public void tbase() {}
}
