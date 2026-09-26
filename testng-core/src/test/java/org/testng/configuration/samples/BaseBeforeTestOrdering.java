package org.testng.configuration.samples;

import org.testng.configuration.BeforeTestOrderingTest;

public class BaseBeforeTestOrdering {

  public void log(String s) {
    BeforeTestOrderingTest.addTest(s);
  }
}
