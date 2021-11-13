package test.reports.issue2069;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

public class Dummy3 extends TestCase {

  @Test
  public void testFails() {
    Assert.assertEquals("test3 assertion that will fail", "1", "2");
  }

  @Test
  public void testSucceeds() {
    Assert.assertEquals("test3 assertion", "1", "1");
  }
}
