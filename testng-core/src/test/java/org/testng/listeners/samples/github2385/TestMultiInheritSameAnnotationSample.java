package org.testng.listeners.samples.github2385;

import org.testng.annotations.Test;

public class TestMultiInheritSameAnnotationSample implements ITestInterface, ITestInterfaceSame {

  @Test
  public void testMultiInheritSameAnnotation() {}
}
