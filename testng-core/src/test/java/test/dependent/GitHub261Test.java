package test.dependent;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.annotations.Test;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

public class GitHub261Test extends SimpleBaseTest {

  @Test
  public void testGitHub261() {
    TestNG tng = create(GitHub261AlphaSample.class, GitHub261BetaSample.class);
    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener(listener);
    tng.setGroupByInstances(true);
    tng.run();

    assertThat(listener.getInvokedMethodNames())
        .containsExactly("testAlpha1", "testAlpha2", "testBeta2", "testBeta1", "testBeta3");
  }
}
