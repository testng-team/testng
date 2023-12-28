package test.annotationtransformer;

import org.testng.IAnnotationTransformer;
import org.testng.annotations.IListenersAnnotation;

public class MyListenerTransformer implements IAnnotationTransformer {

  @Override
  public void transform(IListenersAnnotation annotation, Class testClass) {
    annotation.setValue(new Class[] {MySuiteListener2.class});
  }
}
