package org.testng.internal.annotations;

import org.testng.ITestResult;

public class DefaultAnnotationTransformer 
  implements IAnnotationTransformer 
{

  public ITest transform(ITest annotation, ITestResult result) {
    return annotation;
  }

}
