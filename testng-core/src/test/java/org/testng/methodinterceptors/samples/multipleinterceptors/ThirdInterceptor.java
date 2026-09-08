package org.testng.methodinterceptors.samples.multipleinterceptors;

public class ThirdInterceptor extends MethodNameFilterInterceptor {

  public ThirdInterceptor() {
    super("c");
  }
}
