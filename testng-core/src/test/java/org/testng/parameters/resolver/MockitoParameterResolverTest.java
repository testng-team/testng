package test.inject.parameterresolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.List;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

/**
 * The SPI against a real mocking library rather than a hand written fake.
 *
 * <p>It covers what {@link ParameterResolverTest} cannot: a resolved value that is a generated
 * subclass or proxy instead of an exact instance of the declared type, for an interface and for a
 * class, alongside native injection and a data provider in one signature.
 */
public class MockitoParameterResolverTest extends SimpleBaseTest {

  @Test(description = "GITHUB-1164: a resolver may supply mocks, next to native injection and a DP")
  public void mocksAreResolvedPerInvocation() {
    SampleRun run = SampleRun.of(MockitoSample.class, new MockitoParameterResolver());
    assertThat(run.failureMessages()).isEmpty();

    List<Object[]> invocations = ParameterRecorder.invocationsOf("test");
    assertThat(invocations).hasSize(2);
    assertThat(invocations.get(0)[2]).isEqualTo("Ada");
    assertThat(invocations.get(1)[2]).isEqualTo("Grace");

    for (Object[] parameters : invocations) {
      assertThat(parameters).hasSize(4);
      assertThat(((Method) parameters[0]).getName()).isEqualTo("test");
      assertThat(parameters[1]).isInstanceOf(Greeter.class);
      assertThat(parameters[3]).isInstanceOf(Counter.class);
      assertThat(mockingDetails(parameters[1]).isMock()).isTrue();
      assertThat(mockingDetails(parameters[3]).isMock()).isTrue();
    }

    // A resolved mock is live, not a placeholder: the second invocation gets its own.
    assertThat(invocations.get(0)[1]).isNotSameAs(invocations.get(1)[1]);
    assertThat(invocations.get(0)[3]).isNotSameAs(invocations.get(1)[3]);

    Greeter greeter = (Greeter) invocations.get(0)[1];
    when(greeter.greet("Ada")).thenReturn("hello Ada");
    assertThat(greeter.greet("Ada")).isEqualTo("hello Ada");
    verify(greeter).greet("Ada");
  }
}
