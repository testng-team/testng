package org.testng.methodinterceptors.samples;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

public class Issue392 {

  @AfterClass
  public void afterClass() {}

  @Test
  public void test1() {}

  @Test
  public void test2() {}
}
