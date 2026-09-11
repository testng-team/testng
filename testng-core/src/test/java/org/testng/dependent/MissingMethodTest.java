package org.testng.dependent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.testng.TestNG;
import org.testng.TestNGException;
import org.testng.annotations.Test;
import org.testng.dependent.samples.missingdeps.AlwaysRunMissingMethodSample;
import org.testng.dependent.samples.missingdeps.IgnoredMissingMethodSample;
import org.testng.dependent.samples.missingdeps.PlainMissingMethodSample;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

/**
 * What TestNG does when {@code dependsOnMethods} names a method that does not exist.
 *
 * <p>One case per test. A run stops at the first method it refuses, so a class holding several
 * cases only ever proves the first of them.
 */
public class MissingMethodTest extends SimpleBaseTest {

  @Test
  public void aMissingMethodStopsTheRun() {
    assertThatThrownBy(() -> create(PlainMissingMethodSample.class).run())
        .isInstanceOf(TestNGException.class)
        .hasMessageContaining("dependsOnAMethodThatIsNotThere")
        .hasMessageContaining("depends on nonexistent method");
  }

  @Test
  public void ignoreMissingDependenciesLetsItRun() {
    TestNG tng = create(IgnoredMissingMethodSample.class);
    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener(listener);
    tng.run();

    assertThat(listener.getInvokedMethodNames()).containsExactly("dependsOnAMethodThatIsNotThere");
  }

  @Test
  public void alwaysRunDoesNotLetItRun() {
    // alwaysRun looks like it should help and does not. MethodHelper.findDependedUponMethods skips
    // its own check when isAlwaysRun() is true, so the first gate opens. DependencyMap then throws,
    // because it never asks about alwaysRun at all. The message below comes from DependencyMap,
    // which is how you can tell the first gate opened: the plain case above is refused earlier, by
    // MethodHelper, and its message reads differently.
    assertThatThrownBy(() -> create(AlwaysRunMissingMethodSample.class).run())
        .isInstanceOf(TestNGException.class)
        .hasMessageContaining("dependsOnAMethodThatIsNotThere")
        .hasMessageContaining("depends on nonexistent method");
  }
}
