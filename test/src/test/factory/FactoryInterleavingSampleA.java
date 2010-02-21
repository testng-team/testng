package test.factory;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class FactoryInterleavingSampleA {
  public int m_n;

  public FactoryInterleavingSampleA(int n) {
    m_n = n;
  }

  private void log(Integer s) {
    FactoryInterleavingTest.LOG.add(m_n * 10 + s);
//    System.out.println(" Instance " + m_n + " " + s);
  }

  @Override
  public String toString() {
    return "[A n:" + m_n + "]";
  }

  @BeforeClass
  public void bc() {
    log(0);
  }

  @AfterClass
  public void ac() {
    log(3);
  }

  @Test
  public void f1() {
    log(1);
  }

  @Test
  public void f2() {
    log(2);
  }

}
