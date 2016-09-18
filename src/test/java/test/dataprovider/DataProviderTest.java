package test.dataprovider;

import org.testng.annotations.Test;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class DataProviderTest extends SimpleBaseTest {

  @Test(description = "GITHUB-1139")
  public void oneDimDataProviderShouldWork() {
    InvokedMethodNameListener listener = run(OneDimDataProviderSample.class);

    assertThat(listener.getFailedMethodNames()).isEmpty();
    assertThat(listener.getSkippedMethodNames()).isEmpty();
    assertThat(listener.getSucceedMethodNames()).containsExactly(
        "testArray(foo)", "testArray(bar)",
        "testIterator(foo)", "testIterator(bar)",
        "testStaticArray(foo)", "testStaticArray(bar)",
        "testStaticIterator(foo)", "testStaticIterator(bar)"
    );
  }
}
