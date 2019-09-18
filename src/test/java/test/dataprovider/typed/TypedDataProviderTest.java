package test.dataprovider.typed;

import org.testng.annotations.Test;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TypedDataProviderTest extends SimpleBaseTest {

  @Test
  public void typedDataProviderWorks() {
    InvokedMethodNameListener listener = run(true, TypedDataProviderSample.class);
    assertThat(listener.getSucceedMethodNames())
        .containsExactly(
            "doStuff(test1,1)",
            "doStuff(test2,2)"
        );
    final Throwable throwable = listener.getResult("doStuff2").getThrowable();
    assertThat(throwable).isNotNull();
    assertThat(throwable.getMessage())
            .contains("Wrong method doStuff is called, expected doStuff2");
  }

}
