package org.testng.methodinterceptors.samples.multipleinterceptors;

public class SecondInterceptor extends MethodNameFilterInterceptor {

  public SecondInterceptor() {
    super("b");
  }
}
