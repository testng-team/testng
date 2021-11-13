package test.preserveorder;

import org.testng.annotations.Factory;

public class ClassSampleFactory {

  @Factory
  public Object[] f() {
    final Object[] res = new Object[4];
    for (int i = 0; i < res.length; i++) {
      res[i] = new ClassSample(i + 1);
    }
    return res;
  }
}
