package test.listeners.ordering;

import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * The parallel-data-provider counterpart of {@link
 * SimpleTestClassWithFailedMethodMultipleInvocations}: the same failing method with the same
 * invocation count, cancelled through the other of the two cancellation loops.
 *
 * <p>The data provider yields a single row on purpose. One row means one worker, so the run has a
 * single sequence rather than an interleaving of several, and the listener sees it in an order that
 * is the same every time.
 */
public class SimpleTestClassWithParallelDataDrivenMethodMultipleInvocations {

  @Test(dataProvider = "dp", invocationCount = 2)
  public void testWillFail(int i) {
    fail();
  }

  @DataProvider(name = "dp", parallel = true)
  public static Object[][] getData() {
    return new Object[][] {{1}};
  }
}
