package test.annotationtransformer;

import org.testng.ITestResult;
import org.testng.internal.annotations.IAnnotationTransformer;
import org.testng.internal.annotations.ITest;

public class MyTransformer implements IAnnotationTransformer {

  public ITest transform(ITest annotation, ITestResult result) {
    return new MyTestAnnotation(annotation);
  }

}
