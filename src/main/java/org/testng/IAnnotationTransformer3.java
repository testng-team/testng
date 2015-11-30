package org.testng;

import org.testng.annotations.IListenersAnnotation;

public interface IAnnotationTransformer3 extends IAnnotationTransformer2 {

  void transform(IListenersAnnotation annotation, Class testClass);

}
