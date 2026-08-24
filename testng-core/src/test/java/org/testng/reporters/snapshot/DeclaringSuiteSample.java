package org.testng.reporters.snapshot;

import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Brings a {@link SnapshotReadingReporterSample} into the run with the suite that holds this class,
 * rather than with the run itself. Put it in the <em>second</em> suite to check that the first
 * one's invocations were snapshotted too.
 */
@Listeners(SnapshotReadingReporterSample.class)
public class DeclaringSuiteSample {

  @Test
  public void report() {}
}
