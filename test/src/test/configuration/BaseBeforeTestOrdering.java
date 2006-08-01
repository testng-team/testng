package test.configuration;

public class BaseBeforeTestOrdering {

  public void log(String s) {
    BeforeTestOrderingTest.addTest(s);
  }
}
