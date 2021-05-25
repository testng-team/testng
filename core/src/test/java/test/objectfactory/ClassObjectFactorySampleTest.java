package test.objectfactory;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ClassObjectFactorySampleTest implements ISetValue {

  public static int m_n = 0;

  @Test
  public void f() {
    Assert.assertEquals(m_n, 42);
  }

  @Override
  public void setValue(int i) {
    m_n = i;
  }
}
