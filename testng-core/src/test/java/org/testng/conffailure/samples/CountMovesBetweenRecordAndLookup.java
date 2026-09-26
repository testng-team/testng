package org.testng.conffailure.samples;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** Setup fails. A test moves the shared invocation count after that failure is recorded. */
public class CountMovesBetweenRecordAndLookup {

  @BeforeMethod
  public void setup() {
    throw new IllegalStateException("setup fails");
  }

  @Test
  public void t() {}
}
