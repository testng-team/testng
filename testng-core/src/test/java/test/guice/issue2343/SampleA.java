package test.guice.issue2343;

import javax.inject.Inject;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

@Guice
public class SampleA {

  @Inject
  public SampleA(final Person person) {}

  @Test
  public void testAnotherApp() {}
}
