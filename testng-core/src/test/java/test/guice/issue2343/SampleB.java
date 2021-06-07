package test.guice.issue2343;

import javax.inject.Inject;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

@Guice
public class SampleB {

  @Inject
  public SampleB(final Person person) {}

  @Test
  public void testApp() {}
}
