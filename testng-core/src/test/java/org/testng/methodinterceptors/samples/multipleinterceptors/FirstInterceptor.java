package org.testng.methodinterceptors.samples.multipleinterceptors;

public class FirstInterceptor extends MethodNameFilterInterceptor {

  public FirstInterceptor() {
    super("a");
  }
}
