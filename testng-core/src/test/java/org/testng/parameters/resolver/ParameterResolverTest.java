package org.testng.parameters.resolver;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.List;
import org.testng.ITestContext;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.internal.reflect.MethodMatcherException;
import org.testng.parameters.samples.resolver.CompetingParameterResolver;
import org.testng.parameters.samples.resolver.ConfigurableParameterResolver;
import org.testng.parameters.samples.resolver.CountingParameterResolver;
import org.testng.parameters.samples.resolver.CustomObject;
import org.testng.parameters.samples.resolver.FailsOnSecondResolutionResolver;
import org.testng.parameters.samples.resolver.ListenersAnnotationSample;
import org.testng.parameters.samples.resolver.MethodAnsweringResolver;
import org.testng.parameters.samples.resolver.MultipleResolvedParametersSample;
import org.testng.parameters.samples.resolver.NativeInjectionSample;
import org.testng.parameters.samples.resolver.NoDataProviderSample;
import org.testng.parameters.samples.resolver.NoInjectionSample;
import org.testng.parameters.samples.resolver.OptionalOnResolvedParameterSample;
import org.testng.parameters.samples.resolver.ParameterRecorder;
import org.testng.parameters.samples.resolver.ResolvedAfterDataProviderSample;
import org.testng.parameters.samples.resolver.ResolvedBeforeDataProviderSample;
import org.testng.parameters.samples.resolver.ResolvedBetweenDataProviderValuesSample;
import org.testng.parameters.samples.resolver.RetriedWithCachedRowSample;
import org.testng.parameters.samples.resolver.RetryRereadingItsRowSample;
import org.testng.parameters.samples.resolver.RetryWithFailingDataProviderSample;
import org.testng.parameters.samples.resolver.RetryWithFailingResolverSample;
import org.testng.parameters.samples.resolver.SampleParameterResolver;
import org.testng.parameters.samples.resolver.SampleRun;
import org.testng.parameters.samples.resolver.SecondMethodParameterSample;
import org.testng.parameters.samples.resolver.TooManyDataProviderValuesSample;
import org.testng.parameters.samples.resolver.UnrenderableValue;
import org.testng.parameters.samples.resolver.UnsupportedParameterSample;
import org.testng.parameters.samples.resolver.VarargsSample;
import org.testng.parameters.samples.resolver.XmlParametersSample;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

public class ParameterResolverTest extends SimpleBaseTest {

  @Test(
      description =
          "GITHUB-1164: a resolver supplies a parameter of a method with no data provider")
  public void resolvesWithoutDataProvider() {
    SampleRun run = SampleRun.of(NoDataProviderSample.class, new SampleParameterResolver());
    assertThat(run.failureMessages()).isEmpty();

    Object[] parameters = ParameterRecorder.onlyInvocationOf("test");
    assertThat(parameters).hasSize(1);
    assertThat(parameters[0]).isInstanceOf(CustomObject.class);
  }

  @Test(description = "GITHUB-1164: a resolved parameter does not consume a data provider value")
  public void resolvedParameterBeforeDataProviderValue() {
    SampleRun run =
        SampleRun.of(ResolvedBeforeDataProviderSample.class, new SampleParameterResolver());
    assertThat(run.failureMessages()).isEmpty();

    Object[] parameters = ParameterRecorder.onlyInvocationOf("test");
    assertThat(parameters).hasSize(2);
    assertThat(parameters[0]).isInstanceOf(CustomObject.class);
    assertThat(parameters[1]).isEqualTo("value");
  }

  @Test(description = "GITHUB-1164: the resolved parameter keeps its declared position")
  public void resolvedParameterAfterDataProviderValue() {
    SampleRun run =
        SampleRun.of(ResolvedAfterDataProviderSample.class, new SampleParameterResolver());
    assertThat(run.failureMessages()).isEmpty();

    Object[] parameters = ParameterRecorder.onlyInvocationOf("test");
    assertThat(parameters).hasSize(2);
    assertThat(parameters[0]).isEqualTo("value");
    assertThat(parameters[1]).isInstanceOf(CustomObject.class);
  }

  @Test(description = "GITHUB-1164: a resolved parameter can sit between two data provider values")
  public void resolvedParameterBetweenDataProviderValues() {
    SampleRun run =
        SampleRun.of(ResolvedBetweenDataProviderValuesSample.class, new SampleParameterResolver());
    assertThat(run.failureMessages()).isEmpty();

    Object[] parameters = ParameterRecorder.onlyInvocationOf("test");
    assertThat(parameters).hasSize(3);
    assertThat(parameters[0]).isEqualTo("value");
    assertThat(parameters[1]).isInstanceOf(CustomObject.class);
    assertThat(parameters[2]).isEqualTo(42);
  }

  @Test(description = "GITHUB-1164: one resolver can own several parameters of the same method")
  public void severalResolvedParameters() {
    SampleRun run =
        SampleRun.of(MultipleResolvedParametersSample.class, new SampleParameterResolver());
    assertThat(run.failureMessages()).isEmpty();

    Object[] parameters = ParameterRecorder.onlyInvocationOf("test");
    assertThat(parameters).hasSize(3);
    assertThat(parameters[0]).isInstanceOf(CustomObject.class);
    assertThat(parameters[1]).isEqualTo("value");
    assertThat(parameters[2]).isInstanceOf(CustomObject.class);
  }

  @Test(description = "GITHUB-1164: a resolver coexists with native injection and a data provider")
  public void resolverCoexistsWithNativeInjection() {
    SampleRun run = SampleRun.of(NativeInjectionSample.class, new SampleParameterResolver());
    assertThat(run.failureMessages()).isEmpty();

    Object[] parameters = ParameterRecorder.onlyInvocationOf("test");
    assertThat(parameters).hasSize(4);
    assertThat(parameters[0]).isInstanceOf(Method.class);
    assertThat(((Method) parameters[0]).getName()).isEqualTo("test");
    assertThat(parameters[1]).isInstanceOf(CustomObject.class);
    assertThat(parameters[2]).isEqualTo("value");
    assertThat(parameters[3]).isInstanceOf(ITestContext.class);
  }

  @Test(description = "GITHUB-1164: null is a legal value for a reference typed parameter")
  public void resolverMayAnswerNull() {
    SampleRun run =
        SampleRun.of(
            NoDataProviderSample.class, ConfigurableParameterResolver.answering(p -> null));
    assertThat(run.failureMessages()).isEmpty();

    Object[] parameters = ParameterRecorder.onlyInvocationOf("test");
    assertThat(parameters).hasSize(1);
    assertThat(parameters[0]).isNull();
  }

  @Test(description = "GITHUB-1164: a value the parameter cannot take is a TestNG diagnostic")
  public void incompatibleResolvedValueIsReported() {
    SampleRun run =
        SampleRun.of(
            NoDataProviderSample.class,
            ConfigurableParameterResolver.answering(p -> "not a CustomObject"));

    assertThat(run.failureMessages()).hasSize(1);
    assertThat(run.failureMessages().get(0))
        .contains(ConfigurableParameterResolver.class.getName())
        .contains("resolved parameter 0")
        .contains(CustomObject.class.getName())
        .contains("java.lang.String")
        .contains("not assignable to");
  }

  @Test(description = "GITHUB-1164: two resolvers claiming one parameter is an error, not a race")
  public void competingResolversFailFast() {
    SampleRun run =
        SampleRun.of(
            NoDataProviderSample.class,
            new SampleParameterResolver(),
            new CompetingParameterResolver());

    assertThat(run.failureMessages()).hasSize(1);
    assertThat(run.failureMessages().get(0))
        .contains("More than one IParameterResolver claims")
        .contains("parameter 0")
        .contains(CustomObject.class.getName())
        .contains(SampleParameterResolver.class.getName())
        .contains(CompetingParameterResolver.class.getName())
        .contains(NoDataProviderSample.class.getName() + ".test");
  }

  @Test(description = "GITHUB-1164: a resolver throwing from supportsParameter keeps its cause")
  public void supportsParameterFailurePropagates() {
    SampleRun run =
        SampleRun.of(
            NoDataProviderSample.class,
            new ConfigurableParameterResolver(
                p -> {
                  throw new IllegalStateException("deciding blew up");
                },
                p -> new CustomObject("never")));

    assertThat(run.failureMessages()).hasSize(1);
    assertThat(run.failureMessages().get(0))
        .contains(ConfigurableParameterResolver.class.getName() + ".supportsParameter() failed")
        .contains("parameter 0")
        .contains(NoDataProviderSample.class.getName() + ".test");
    assertThat(causeOfOnlyFailure(run)).hasMessage("deciding blew up");
  }

  @Test(description = "GITHUB-1164: a resolver throwing from resolveParameter keeps its cause")
  public void resolveParameterFailurePropagates() {
    SampleRun run =
        SampleRun.of(
            NoDataProviderSample.class,
            ConfigurableParameterResolver.answering(
                p -> {
                  throw new IllegalStateException("resolving blew up");
                }));

    assertThat(run.failureMessages()).hasSize(1);
    assertThat(run.failureMessages().get(0))
        .contains(ConfigurableParameterResolver.class.getName() + ".resolveParameter() failed")
        .contains("parameter 0")
        .contains(NoDataProviderSample.class.getName() + ".test");
    assertThat(causeOfOnlyFailure(run)).hasMessage("resolving blew up");
  }

  @Test(
      description =
          "GITHUB-1164: a parameter no resolver claims keeps the existing mismatch diagnostic")
  public void unsupportedParameterKeepsExistingBehaviour() {
    SampleRun run = SampleRun.of(UnsupportedParameterSample.class, new SampleParameterResolver());

    assertThat(run.failureMessages()).hasSize(1);
    assertThat(run.failureMessages().get(0))
        .contains(MethodMatcherException.class.getName())
        // The count is the method's own: the CustomObject nobody claims is still the provider's.
        .contains("Data provider mismatch: expected 2 arguments, got 1");
    assertThat(ParameterRecorder.invocationsOf("test")).isEmpty();
  }

  @Test(description = "GITHUB-1164: @NoInjection still hands the parameter to the data provider")
  public void noInjectionIsUnchanged() {
    SampleRun run = SampleRun.of(NoInjectionSample.class, new SampleParameterResolver());
    assertThat(run.failureMessages()).isEmpty();

    Object[] parameters = ParameterRecorder.onlyInvocationOf("test");
    assertThat(parameters).hasSize(1);
    assertThat(((Method) parameters[0]).getName()).isEqualTo("aMethodFromTheDataProvider");
  }

  @Test(description = "GITHUB-1164: a resolver can be registered with @Listeners")
  public void resolverRegisteredWithListenersAnnotation() {
    SampleRun run = SampleRun.of(ListenersAnnotationSample.class);
    assertThat(run.failureMessages()).isEmpty();

    Object[] parameters = ParameterRecorder.onlyInvocationOf("test");
    assertThat(parameters).hasSize(1);
    assertThat(parameters[0]).isInstanceOf(CustomObject.class);
  }

  @Test(
      description =
          "GITHUB-1164: a resolver failure on a data driven method fails that method, not the run")
  public void resolverFailureOnDataDrivenMethodFailsTheMethod() {
    SampleRun run =
        SampleRun.of(
            ResolvedBeforeDataProviderSample.class,
            ConfigurableParameterResolver.answering(
                p -> {
                  throw new IllegalStateException("resolving blew up");
                }));

    assertThat(run.failureMessages()).hasSize(1);
    assertThat(run.failureMessages().get(0))
        .contains(ConfigurableParameterResolver.class.getName() + ".resolveParameter() failed");
    assertThat(ParameterRecorder.invocationsOf("test")).isEmpty();
  }

  @Test(
      description =
          "GITHUB-1164: competing resolvers on a data driven method fail that method, not the run")
  public void competingResolversOnDataDrivenMethodFailTheMethod() {
    SampleRun run =
        SampleRun.of(
            ResolvedBeforeDataProviderSample.class,
            new SampleParameterResolver(),
            new CompetingParameterResolver());

    assertThat(run.failureMessages()).hasSize(1);
    assertThat(run.failureMessages().get(0)).contains("More than one IParameterResolver claims");
    assertThat(ParameterRecorder.invocationsOf("test")).isEmpty();
  }

  @Test(description = "GITHUB-1164: the varargs matcher works off the filtered parameter set")
  public void resolvedParameterBeforeVarargs() {
    SampleRun run = SampleRun.of(VarargsSample.class, new SampleParameterResolver());
    assertThat(run.failureMessages()).isEmpty();

    Object[] parameters = ParameterRecorder.onlyInvocationOf("test");
    assertThat(parameters).hasSize(2);
    assertThat(parameters[0]).isInstanceOf(CustomObject.class);
    assertThat((String[]) parameters[1]).containsExactly("one", "two", "three");
  }

  @Test(
      description =
          "GITHUB-1164: a retry that re-reads its row keeps the resolved value, and the natively"
              + " injected one with it")
  public void retryRereadingItsRowKeepsInjectedValues() {
    SampleRun run = SampleRun.of(RetryRereadingItsRowSample.class, new SampleParameterResolver());

    // Both methods fail on purpose, so both are retried once. What matters is that the second
    // attempt reached the body at all: it only can if the re-read row went back through the
    // matcher. The control method uses native injection alone, which is why this is not a
    // resolver-only fix.
    assertThat(ParameterRecorder.invocationsOf("nativeControl")).hasSize(2);
    assertThat(ParameterRecorder.invocationsOf("withResolver")).hasSize(2);
    assertThat(run.failureMessages())
        .noneSatisfy(message -> assertThat(message).contains("wrong number of arguments"));

    for (Object[] parameters : ParameterRecorder.invocationsOf("nativeControl")) {
      assertThat(parameters[0]).isEqualTo("value");
      assertThat(parameters[1]).isInstanceOf(ITestContext.class);
    }
    for (Object[] parameters : ParameterRecorder.invocationsOf("withResolver")) {
      assertThat(parameters[0]).isInstanceOf(CustomObject.class);
      assertThat(parameters[1]).isEqualTo("value");
    }
  }

  @Test(description = "GITHUB-1164: the diagnostic survives a value whose toString() throws")
  public void incompatibleValueThatCannotRenderItself() {
    SampleRun run =
        SampleRun.of(
            NoDataProviderSample.class,
            ConfigurableParameterResolver.answering(p -> new UnrenderableValue()));

    assertThat(run.failureMessages()).hasSize(1);
    assertThat(run.failureMessages().get(0))
        .contains(UnrenderableValue.class.getName())
        .contains("not assignable to")
        .contains(CustomObject.class.getName())
        .doesNotContain("toString() blew up");
  }

  @Test(
      description =
          "GITHUB-1164: an @Optional on a resolved parameter does not excuse the ones nobody owns")
  public void optionalOnResolvedParameterStillChecksTheOthers() {
    SampleRun run =
        SampleRun.of(OptionalOnResolvedParameterSample.class, new SampleParameterResolver());

    // The int is nobody's to supply, so the native injection check must still reject it, and name
    // it. Reading the unfiltered optional values would skip that check and leave the method
    // failing later with a data provider mismatch it never had a data provider for.
    assertThat(run.failureMessages()).hasSize(1);
    assertThat(run.failureMessages().get(0))
        .contains("Cannot inject @Test annotated Method [test]")
        .contains("int");
    assertThat(ParameterRecorder.invocationsOf("test")).isEmpty();
  }

  @Test(
      description =
          "GITHUB-1164: a resolved parameter sits beside one named by @Parameters, in either order")
  public void resolvedParameterBesideAnXmlParameter() {
    // @Parameters names only what testng.xml supplies, so its length is already the reduced count
    // -- one name for two declared parameters. Filtering the resolver out of the types must keep
    // that pairing, which is what indexes the value onto the right parameter.
    XmlSuite suite = createXmlSuite("suite");
    XmlTest xmlTest =
        createXmlTest(suite, "test", java.util.Collections.singletonMap("greeting", "bonjour"));
    createXmlClass(xmlTest, XmlParametersSample.class);

    ParameterRecorder.clear();
    TestNG testng = create(suite);
    TestListenerAdapter adapter = new TestListenerAdapter();
    testng.addListener(adapter);
    testng.addListener(new SampleParameterResolver());
    testng.run();

    assertThat(adapter.getFailedTests()).isEmpty();

    Object[] forward = ParameterRecorder.onlyInvocationOf("test");
    assertThat(forward[0]).isInstanceOf(CustomObject.class);
    assertThat(forward[1]).isEqualTo("bonjour");

    Object[] reversed = ParameterRecorder.onlyInvocationOf("reversed");
    assertThat(reversed[0]).isEqualTo("bonjour");
    assertThat(reversed[1]).isInstanceOf(CustomObject.class);
  }

  @Test(
      description =
          "GITHUB-1164: a mismatch diagnostic counts only what the data provider has to supply")
  public void mismatchDiagnosticLeavesTheResolvedParameterOut() {
    // ResolvedBeforeDataProviderSample declares (CustomObject, String) and its provider supplies
    // one value; a resolver that refuses the CustomObject makes that row one short of two.
    // Register one that owns it and hand the provider's row the wrong number: the message must
    // name one expected argument, not two.
    SampleRun run =
        SampleRun.of(TooManyDataProviderValuesSample.class, new SampleParameterResolver());

    assertThat(run.failureMessages()).hasSize(1);
    assertThat(run.failureMessages().get(0))
        .contains("Data provider mismatch: expected 1 argument, got 2");
    assertThat(ParameterRecorder.invocationsOf("test")).isEmpty();
  }

  @Test(
      description =
          "GITHUB-1164: a resolver that breaks on a retry fails that retry, and nothing else")
  public void resolverFailingOnRetryFailsTheRetry() {
    SampleRun run =
        SampleRun.of(RetryWithFailingResolverSample.class, new FailsOnSecondResolutionResolver());

    // The retry re-reads its row and re-resolves the parameter, which is where this resolver
    // throws. That must surface as the method's failure, with the resolver named, and must not
    // take the run down: the sibling method still executes.
    assertThat(run.failureMessages()).hasSize(1);
    assertThat(run.failureMessages().get(0))
        .contains(FailsOnSecondResolutionResolver.class.getName() + ".resolveParameter() failed")
        .contains(RetryWithFailingResolverSample.class.getName() + ".test");
    assertThat(ParameterRecorder.invocationsOf("test")).hasSize(1);
    assertThat(ParameterRecorder.invocationsOf("sibling")).hasSize(1);
  }

  @Test(
      description =
          "A retry whose data provider cannot rebuild the row reports that, instead of vanishing")
  public void dataProviderFailingOnRetryIsReported() {
    ParameterRecorder.clear();
    TestNG testng = create(RetryWithFailingDataProviderSample.class);
    TestListenerAdapter adapter = new TestListenerAdapter();
    testng.addListener(adapter);
    testng.run();

    // No resolver here. Before, the retry left the loop with nothing recorded, so the method --
    // which had failed -- ended up reported as one skip and no failure. The provider's exception
    // is now the result of the retry itself, with the status a first attempt would have been given.
    assertThat(adapter.getFailedTests()).isEmpty();
    assertThat(adapter.getSkippedTests()).hasSize(2);
    assertThat(adapter.getSkippedTests())
        .anySatisfy(
            result ->
                assertThat(result.getThrowable())
                    .hasMessageContaining("provider broke on the retry"));
    assertThat(ParameterRecorder.invocationsOf("test")).hasSize(1);
  }

  @Test(
      description =
          "GITHUB-1164: every retry is an invocation of its own, so it is resolved afresh -- with"
              + " the row cached, and with no data provider at all")
  public void retryResolvesAfresh() {
    CountingParameterResolver resolver = new CountingParameterResolver();
    SampleRun.of(RetriedWithCachedRowSample.class, resolver);

    for (String method : new String[] {"withDataProvider", "withoutDataProvider"}) {
      List<Object[]> attempts = ParameterRecorder.invocationsOf(method);
      assertThat(attempts).as(method).hasSize(2);
      assertThat(attempts.get(0)[0])
          .as(method + " retry gets its own value")
          .isNotSameAs(attempts.get(1)[0]);
    }
    assertThat(resolver.answers()).as("one resolution per attempt").hasSize(4);
  }

  @Test(
      description =
          "GITHUB-1164: TestNG injects only the first Method parameter, so a resolver may own the"
              + " second")
  public void secondMethodParameterIsResolvable() {
    SampleRun run = SampleRun.of(SecondMethodParameterSample.class, new MethodAnsweringResolver());
    assertThat(run.failureMessages()).isEmpty();

    Object[] parameters = ParameterRecorder.onlyInvocationOf("test");
    assertThat(((Method) parameters[0]).getName()).isEqualTo("test");
    assertThat(parameters[1]).isSameAs(MethodAnsweringResolver.ANSWER);
  }

  private static Throwable causeOfOnlyFailure(SampleRun run) {
    assertThat(run.failed()).hasSize(1);
    Throwable thrown = run.failed().get(0).getThrowable();
    assertThat(thrown).isNotNull();
    Throwable cause = thrown.getCause();
    assertThat(cause).as("the resolver failure keeps the original as its cause").isNotNull();
    return cause;
  }
}
