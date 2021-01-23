package test.annotationtransformer.issue2312;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

public class RetryListener implements IAnnotationTransformer {

  private static int executedNrOfTimes = 0;

  public static int getExecutionCount() {
    return executedNrOfTimes;
  }

  public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor,
      Method testMethod) {
    executedNrOfTimes++;
  }

}