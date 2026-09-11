package org.testng.dependent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.testng.TestNG;
import org.testng.TestNGException;
import org.testng.annotations.Test;
import org.testng.dependent.samples.missingdeps.AlwaysRunMissingGroupSample;
import org.testng.dependent.samples.missingdeps.IgnoredMissingGroupSample;
import org.testng.dependent.samples.missingdeps.PlainMissingGroupSample;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

/**
 * What TestNG does when {@code dependsOnGroups} names a group no method declares.
 *
 * <p>One case per test. A run stops at the first method it refuses, so a class holding several
 * cases only ever proves the first of them.
 */
public class MissingGroupTest extends SimpleBaseTest {

  @Test
  public void aMissingGroupStopsTheRun() {
    assertThatThrownBy(() -> create(PlainMissingGroupSample.class).run())
        .isInstanceOf(TestNGException.class)
        .hasMessageContaining("dependsOnAGroupThatIsNotThere")
        .hasMessageContaining("depends on nonexistent group");
  }

  @Test
  public void ignoreMissingDependenciesLetsItRun() {
    TestNG tng = create(IgnoredMissingGroupSample.class);
    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener(listener);
    tng.run();

    assertThat(listener.getInvokedMethodNames()).containsExactly("dependsOnAGroupThatIsNotThere");
  }

  @Test
  public void alwaysRunDoesNotLetItRun() {
    // Same as the method case. DependencyMap.getMethodsThatBelongTo asks about
    // ignoreMissingDependencies and never about alwaysRun, so alwaysRun does not help here either.
    assertThatThrownBy(() -> create(AlwaysRunMissingGroupSample.class).run())
        .isInstanceOf(TestNGException.class)
        .hasMessageContaining("dependsOnAGroupThatIsNotThere")
        .hasMessageContaining("depends on nonexistent group");
  }
}
