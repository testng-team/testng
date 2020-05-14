package test.verify;

import org.testng.annotations.BeforeClass;

/**
 * Make sure that @Listeners annotations can come from superclasses
 */
public class Verify3SampleTest extends Verify3Base {
  @BeforeClass
  public void bc() {
    VerifyTestListener.m_count = 0;
  }
}
