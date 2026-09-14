package org.testng.factory.samples.lazy;

import java.util.function.IntSupplier;
import org.testng.ISuite;
import org.testng.ISuiteListener;

/**
 * Captures how many factory instances had been constructed at the very moment the suite starts —
 * i.e. after collection but before any configuration or test method runs. Lets a test assert
 * directly that lazy instantiation creates <em>nothing</em> up-front, rather than inferring it from
 * run-time ordering.
 */
public class InstancesConstructedAtSuiteStart implements ISuiteListener {

  private final IntSupplier constructedCount;
  private volatile int countAtStart = -1;

  public InstancesConstructedAtSuiteStart(IntSupplier constructedCount) {
    this.constructedCount = constructedCount;
  }

  @Override
  public void onStart(ISuite suite) {
    countAtStart = constructedCount.getAsInt();
  }

  public int countAtStart() {
    return countAtStart;
  }
}
