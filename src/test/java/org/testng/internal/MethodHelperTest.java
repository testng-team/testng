package org.testng.internal;

import static org.testng.Assert.fail;

import org.testng.ITestNGMethod;
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
            testClass);
    ITestNGMethod[] methods = new ITestNGMethod[0];
    method.addMethodDependedUpon("dummyDependsOnMethod");
    MethodHelper.findDependedUponMethods(method, methods);
    fail("Should have risen a nice exception");
  }
}
