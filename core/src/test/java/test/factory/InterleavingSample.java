package test.factory;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class InterleavingSample {
  public final int n;

  public InterleavingSample(int n) {
    this.n = n;
  }

  private void log(Integer s) {
    FactoryInterleavingTest.LOG.add(n * 10 + s);
  }

  @Override
  public String toString() {
    return "[A n:" + n + "]";
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
