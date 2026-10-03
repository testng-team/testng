package org.testng.listeners;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.listeners.samples.ResultContextListener;
import org.testng.listeners.samples.ResultContextListenerSample;
import test.SimpleBaseTest;

public class ResultContextTest extends SimpleBaseTest {

  @Test
  public void testResultContext() {
    ResultContextListener.contextStarted = null;
    ResultContextListener.contextOnResult = null;
    TestNG tng = create(ResultContextListenerSample.class);
    tng.run();
    // The context on the result has to be the context the run started, not merely one that is not
    // null. Every result a listener is given carries the context of the run, so a null one never
    // arrives, and a wrong one would look the same from the outside.
    // Both start null. Without this the next check passes when the listener never runs at all,
    // because one null is the same object as another.
    assertThat(ResultContextListener.contextStarted)
        .withFailMessage("The listener was never called, so there is nothing to compare")
        .isNotNull();
    assertThat(ResultContextListener.contextOnResult)
        .withFailMessage("The listener did not receive the context the run started")
        .isSameAs(ResultContextListener.contextStarted);
  }
}
