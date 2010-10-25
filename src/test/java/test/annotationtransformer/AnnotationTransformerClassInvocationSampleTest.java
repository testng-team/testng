package test.annotationtransformer;

import org.testng.annotations.Test;

@Test(invocationCount = 3)
public class AnnotationTransformerClassInvocationSampleTest {

  public void f1() {}

  public void f2() {}
}
