package test.annotationtransformer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;
import org.testng.annotations.Test;

public class AnnotationTransformerInTestngXml implements IAnnotationTransformer {

  @Test(enabled = false)
  public void shouldRunAfterTransformation() {}

  @Override
  public void transform(
      ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
    annotation.setEnabled(true);
  }
}
