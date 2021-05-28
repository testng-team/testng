package test.thread;

import org.testng.annotations.Factory;
import org.testng.annotations.Test;


@Test
public class FactorySampleTest {

  @Factory
  public Object[] init() {
    return new Object[] {
        new B(),
        new B(),
    };
  }
}

