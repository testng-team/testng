package org.testng.inheritance.samples.github949;

import org.testng.annotations.Test;

public class ChildClassWithAlwasyRunEnabledSample extends ParentClassWithAlwasyRunEnabledSample {

  @Override
  @Test
  public void independent() {
    logMessage();
  }
}
