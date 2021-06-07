package org.testng.junit;

import org.testng.ITestNGMethod;
import org.testng.ITestObjectFactory;
import org.testng.internal.BaseTestMethod;
import org.testng.internal.ConstructorOrMethod;

// NO JUnit specific code here to avoid runtime errors
public abstract class JUnitTestMethod extends BaseTestMethod {

  protected JUnitTestMethod(
      ITestObjectFactory objectFactory,
      JUnitTestClass owner,
      ConstructorOrMethod method,
      Object instance) {
    this(objectFactory, owner, method.getName(), method, instance);
  }

  protected JUnitTestMethod(
      ITestObjectFactory objectFactory,
      JUnitTestClass owner,
      String methodName,
      ConstructorOrMethod method,
      Object instance) {
    super(objectFactory, methodName, method, null, instance);
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
