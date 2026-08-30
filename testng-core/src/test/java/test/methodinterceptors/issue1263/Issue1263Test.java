package test.methodinterceptors.issue1263;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.stream.Collectors;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;
import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.TestNGException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

/**
 * An {@link IMethodInterceptor} is handed every test method of its {@code <test>}, the ones taking
 * part in a dependency included -- which is what GITHUB-1263 reported against a javadoc promising
 * the opposite. What the interceptor returns is what the scheduling graph is built from, so a
 * method withheld here would be a method no implementation could drop; what each method is bound to
 * is made visible instead, and these tests pin both halves of that.
 */
public class Issue1263Test extends SimpleBaseTest {

  @DataProvider(name = "dependencySamples")
  public Object[][] dependencySamples() {
    return new Object[][] {{DependsOnMethodsSample.class}, {DependsOnGroupsSample.class}};
  }

  @Test(description = "GITHUB-1263", dataProvider = "dependencySamples")
  public void aDeclaredDependencyIsVisibleToAnInterceptor(Class<?> sample) {
    RecordingInterceptor interceptor = record(sample);

    assertThat(interceptor.received())
        .containsExactlyInAnyOrder("independent", "prerequisite", "dependent");
    assertThat(interceptor.upstreamOf("independent")).isEmpty();
    assertThat(interceptor.downstreamOf("independent")).isEmpty();
    assertThat(interceptor.upstreamOf("prerequisite")).isEmpty();
    assertThat(interceptor.downstreamOf("prerequisite")).containsExactly("dependent");
    assertThat(interceptor.upstreamOf("dependent")).containsExactly("prerequisite");
    assertThat(interceptor.downstreamOf("dependent")).isEmpty();
  }

  @Test(
      description = "GITHUB-1263: the sets answer the scheduling order, not only what is declared")
  public void anOrderTestNGDerivedItselfIsVisibleToo() {
    RecordingInterceptor interceptor = record(TwoClassSampleA.class, TwoClassSampleB.class);

    // preserve-order is on by default, so the second class runs after the first and the graph says
    // so -- with no dependsOnMethods anywhere in the suite.
    assertThat(interceptor.upstreamOf("first")).isEmpty();
    assertThat(interceptor.downstreamOf("first")).containsExactly("second");
    assertThat(interceptor.upstreamOf("second")).containsExactly("first");
    assertThat(interceptor.downstreamOf("second")).isEmpty();
  }

  @Test(description = "GITHUB-1263: what the javadoc warns a removal costs")
  public void removingAMethodThatOthersDeclareADependencyUponEndsTheRun() {
    assertThatThrownBy(() -> runWithout("prerequisite", DependsOnMethodsSample.class))
        .isInstanceOf(TestNGException.class)
        .hasMessageContaining("dependent() is depending on method")
        .hasMessageContaining("prerequisite(), which is not annotated with @Test or not included");
  }

  @Test(description = "GITHUB-1263: removing the dependent end of the pair is fine")
  public void removingADependentMethodLeavesTheRestOfTheRunAlone() {
    assertThat(runWithout("dependent", DependsOnMethodsSample.class))
        .containsExactlyInAnyOrder("independent", "prerequisite");
  }

  @Test(description = "GITHUB-1263: an order TestNG derived itself is safe to drop")
  public void removingAMethodOnlyPreserveOrderBindsIsFine() {
    assertThat(runWithout("first", TwoClassSampleA.class, TwoClassSampleB.class))
        .containsExactly("second");
  }

  private static RecordingInterceptor record(Class<?>... samples) {
    RecordingInterceptor interceptor = new RecordingInterceptor();
    TestNG tng = create(samples);
    tng.addListener((ITestNGListener) interceptor);
    tng.run();
    return interceptor;
  }

  private static List<String> runWithout(String methodName, Class<?>... samples) {
    TestNG tng = create(samples);
    tng.addListener((ITestNGListener) new RemovingInterceptor(methodName));
    return run(false, tng).getInvokedMethodNames();
  }

  private static class RemovingInterceptor implements IMethodInterceptor {

    private final String methodName;

    RemovingInterceptor(String methodName) {
      this.methodName = methodName;
    }

    @Override
    public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
      return methods.stream()
          .filter(each -> !each.getMethod().getMethodName().equals(methodName))
          .collect(Collectors.toList());
    }
  }
}
