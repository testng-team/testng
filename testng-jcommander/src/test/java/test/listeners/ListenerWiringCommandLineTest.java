package test.listeners;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import test.listeners.cliwiring.FirstWiringListener;
import test.listeners.cliwiring.ReverseNameListenerComparator;
import test.listeners.cliwiring.SecondWiringListener;
import test.listeners.cliwiring.WiringLog;
import test.listeners.cliwiring.WiringSampleTest;

/**
 * Covers the {@code -listener} / {@code -listenercomparator} / {@code -usedefaultlisteners} wiring
 * end to end. {@code testng-core} exercises listener ordering through the Java API; only the
 * command line goes through comma splitting and class loading, so that part is pinned here.
 */
public class ListenerWiringCommandLineTest {

  @BeforeMethod
  public void clearLog() {
    WiringLog.clear();
  }

  @Test
  public void commaSeparatedListenersAreWiredAndOrderedByTheComparator() {
    String[] args = {
      "-listener",
      FirstWiringListener.class.getName() + "," + SecondWiringListener.class.getName(),
      "-listenercomparator",
      ReverseNameListenerComparator.class.getName(),
      "-usedefaultlisteners",
      "false",
      "-testclass",
      WiringSampleTest.class.getName()
    };

    TestNG testng = TestNG.privateMain(args, null);

    assertThat(testng.getStatus()).isZero();
    // Both names made it through the comma split, and the comparator drove the order:
    // "Second..." sorts after "First...", and the comparator reverses that.
    assertThat(WiringLog.entries()).containsExactly("second", "first");
  }

  @Test
  public void semicolonSeparatedListenersAreAlsoAccepted() {
    String[] args = {
      "-listener",
      FirstWiringListener.class.getName() + ";" + SecondWiringListener.class.getName(),
      "-usedefaultlisteners",
      "false",
      "-testclass",
      WiringSampleTest.class.getName()
    };

    TestNG testng = TestNG.privateMain(args, null);

    assertThat(testng.getStatus()).isZero();
    assertThat(WiringLog.entries()).containsExactlyInAnyOrder("first", "second");
  }
}
