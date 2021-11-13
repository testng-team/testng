package test.ignore.issue2396;

import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

@Test
public class FirstTest {

  public void testShouldBeInvoked() {}

  @Ignore
  public void testShouldBeIgnored(String name) {}
}
