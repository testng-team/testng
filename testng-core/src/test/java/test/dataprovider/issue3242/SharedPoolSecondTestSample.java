package test.dataprovider.issue3242;

import org.testng.annotations.Test;

/**
 * Second {@code <test>} of the GITHUB-3242 shared-pool-across-tests scenario. Its method used to be
 * silently dropped (its worker rejected) when the first {@code <test>} shut the shared pool down.
 */
public class SharedPoolSecondTestSample {

  @Test
  public void second() {}
}
