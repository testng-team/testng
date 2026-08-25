package test.guice.issue2343;

import javax.inject.Inject;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

@Guice
public class SampleB {

  // Guice must be able to inject this; nothing needs to read it.
  @Inject
  @SuppressWarnings("unused")
  public SampleB(final Person person) {}

  @Test
  public void testApp() {}
}
