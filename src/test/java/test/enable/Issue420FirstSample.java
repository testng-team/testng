package test.enable;

import org.testng.annotations.Test;

@Test(enabled = false)
public class Issue420FirstSample extends Issue420BaseTestCase {

  @Test
  public void verifySomethingFirstSample() {}
}
