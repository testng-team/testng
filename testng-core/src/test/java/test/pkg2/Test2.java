package test.pkg2;

import test.pkg.PackageTest;

public class Test2 {
  // Never called, and the parameter is never read -- both on purpose, so both checks are named
  // rather than left to the "unused" alias. PackageTest asserts this constructor does not run: it
  // is how the test detects TestNG instantiating a class that holds no test methods. Dropping the
  // parameter would turn it into a no-arg constructor and change what that assertion is exposed to.
  @SuppressWarnings({"UnusedVariable", "UnusedMethod"})
  private Test2(float afloat) {
    PackageTest.NON_TEST_CONSTRUCTOR = true;
  }

  public void nonTestMethod() {}
}
