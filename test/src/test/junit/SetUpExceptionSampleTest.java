package test.junit;

import junit.framework.TestCase;

public class SetUpExceptionSampleTest extends TestCase {

  @Override
  protected void setUp() throws Exception {
    throw new RuntimeException();
  }

  public void testM1() {
  }

  @Override
  protected void tearDown() throws Exception {
  }

}