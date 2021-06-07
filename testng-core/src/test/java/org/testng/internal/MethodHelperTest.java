package org.testng.internal;

import org.testng.ITestNGMethod;
import org.testng.ITestObjectFactory;
import org.testng.Reporter;
import org.testng.TestNGException;
import org.testng.annotations.Test;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.annotations.JDK15AnnotationFinder;
import org.testng.internal.issue2195.TestClass;
import test.tmp.AnnotationTransformer;

public class MethodHelperTest {

  @Test(expectedExceptions = {TestNGException.class})
  public void findDependedUponMethods() throws NoSuchMethodException {

    TestClass testClass = new TestClass();
    ConstructorOrMethod constructorOrMethod =
        new ConstructorOrMethod(testClass.getClass().getMethod("dummyMethod"));
    IAnnotationFinder annotationFinder = new JDK15AnnotationFinder(new AnnotationTransformer());
    ITestNGMethod method =
        new ConfigurationMethod(
            new ITestObjectFactory() {},
            constructorOrMethod,
            annotationFinder,
            false,
            false,
            false,
            false,
            false,
            false,
            true,
            false,
            new String[0],
            new String[0],
            Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest(),
            testClass);
    method.addMethodDependedUpon("dummyDependsOnMethod");
    ITestNGMethod[] methods = new ITestNGMethod[0];

    MethodHelper.findDependedUponMethods(method, methods);
  }
}
