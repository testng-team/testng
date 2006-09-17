package org.testng.internal.annotations;

import org.testng.ITestResult;

public interface IAnnotationTransformer {

  public ITest transform(ITest annotation, ITestResult result);
  
}
