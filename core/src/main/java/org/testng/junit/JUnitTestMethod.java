package org.testng.junit;

import org.testng.ITestNGMethod;
import org.testng.internal.BaseTestMethod;
import org.testng.internal.ConstructorOrMethod;

/** @author lukas */
// NO JUnit specific code here to avoid runtime errors
public abstract class JUnitTestMethod extends BaseTestMethod {

  protected JUnitTestMethod(JUnitTestClass owner, ConstructorOrMethod method, Object instance) {
    this(owner, method.getName(), method, instance);
  }

  protected JUnitTestMethod(
      JUnitTestClass owner, String methodName, ConstructorOrMethod method, Object instance) {
    super(methodName, method, null, instance);
    setTestClass(owner);
    owner.getTestMethodList().add(this);
  }

  @Override
  public boolean isTest() {
    return true;
  }

  @Override
  public ITestNGMethod clone() {
    throw new IllegalStateException("clone is not supported for JUnit");
  }
}
