package test.annotationtransformer.issue2312;

import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class RetryListener implements IAnnotationTransformer {

  private static int executedNrOfTimes = 0;

  public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor,
                        Method testMethod) {
    executedNrOfTimes++;
  }

  public static int getExecutionCount() {
    return executedNrOfTimes;
  }

}