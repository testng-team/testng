package test.annotationtransformer;

import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;
import org.testng.annotations.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class AnnotationTransformerInvocationCountTest {

  public static class InvocationCountTransformer implements IAnnotationTransformer {

    private final int invocationCount;

    public InvocationCountTransformer(int invocationCount) {
      this.invocationCount = invocationCount;
    }

    @Override
    public void transform(ITestAnnotation annotation, Class testClass,
                          Constructor testConstructor, Method testMethod) {
      if ("concurrencyTest".equals(testMethod.getName())) {
        annotation.setInvocationCount(invocationCount);
      }
    }
  }

  @Test(invocationCount = 3)
  public void concurrencyTest() {
  }
}
