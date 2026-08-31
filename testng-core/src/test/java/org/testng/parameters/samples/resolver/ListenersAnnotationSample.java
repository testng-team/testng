package test.inject.parameterresolver;

import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** Registration through {@code @Listeners} rather than programmatically. */
@Listeners(SampleParameterResolver.class)
public class ListenersAnnotationSample {

  @Test
  public void test(@FromResolver CustomObject custom) {
    ParameterRecorder.record("test", custom);
  }
}
