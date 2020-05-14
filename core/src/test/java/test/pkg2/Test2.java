package test.pkg2;

import test.pkg.PackageTest;


public class Test2 {
  private Test2(float afloat) {
    PackageTest.NON_TEST_CONSTRUCTOR= true;
  }

  public void nonTestMethod() {
  }
}
