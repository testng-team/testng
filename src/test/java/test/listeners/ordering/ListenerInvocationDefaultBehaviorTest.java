package test.listeners.ordering;

import static test.listeners.ordering.Constants.*;

import java.util.Arrays;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class ListenerInvocationDefaultBehaviorTest extends SimpleBaseTest {

  //Test methods along with configurations

  @Test(description = "Test class has only 1 passed config and test method")
  public void testOrderHasOnlyPassedConfigAndTestMethod() {
    List<String> expected = Arrays.asList(
        IEXECUTIONLISTENER_ON_EXECUTION_START,
        IALTERSUITELISTENER_ALTER,
        IANNOTATIONTRANSFORMER_TRANSFORM_3_ARGS,
        ISUITELISTENER_ON_START,
        ITESTLISTENER_ON_START_TEST_TAG,
        METHODINTERCEPTOR_INTERCEPT,
        METHODINTERCEPTOR_INTERCEPT,
        ICLASSLISTENER_ON_BEFORE_CLASS,
        ICONFIGURATIONLISTENER_BEFORE_CONFIGURATION,
        IINVOKEDMETHODLISTENER_BEFORE_INVOCATION,
        IINVOKEDMETHODLISTENER_AFTER_INVOCATION,
        ICONFIGURATIONLISTENER_ON_CONFIGURATION_SUCCESS,
        ITESTLISTENER_ON_START_TEST_METHOD,
        IINVOKEDMETHODLISTENER_BEFORE_INVOCATION,
        IINVOKEDMETHODLISTENER_AFTER_INVOCATION,
        ITESTLISTENER_ON_TEST_SUCCESS_TEST_METHOD,
        ICLASSLISTENER_ON_AFTER_CLASS,
        IEXECUTION_VISUALISER_CONSUME_DOT_DEFINITION,
        ITESTLISTENER_ON_FINISH_TEST_TAG,
        ISUITELISTENER_ON_FINISH,
        IREPORTER_GENERATE_REPORT,
        IEXECUTIONLISTENER_ON_EXECUTION_FINISH
    );
    runTest(expected, SimpleTestClassWithPassConfigAndMethod.class);
  }

  @Test(description = "Test class has only 1 failed config and skipped test method")
  public void testOrderHasOnlyFailedConfigAndSkippedTestMethod() {
    List<String> expected = Arrays.asList(
        IEXECUTIONLISTENER_ON_EXECUTION_START,
        IALTERSUITELISTENER_ALTER,
        IANNOTATIONTRANSFORMER_TRANSFORM_3_ARGS,
        ISUITELISTENER_ON_START,
        ITESTLISTENER_ON_START_TEST_TAG,
        METHODINTERCEPTOR_INTERCEPT,
        METHODINTERCEPTOR_INTERCEPT,
        ICLASSLISTENER_ON_BEFORE_CLASS,
        ICONFIGURATIONLISTENER_BEFORE_CONFIGURATION,
        IINVOKEDMETHODLISTENER_BEFORE_INVOCATION,
        IINVOKEDMETHODLISTENER_AFTER_INVOCATION,
        ICONFIGURATIONLISTENER_ON_CONFIGURATION_FAILURE,
        ITESTLISTENER_ON_START_TEST_METHOD,
        IINVOKEDMETHODLISTENER_BEFORE_INVOCATION,
        IINVOKEDMETHODLISTENER_AFTER_INVOCATION,
        ITESTLISTENER_ON_TEST_SKIPPED_TEST_METHOD,
        ICLASSLISTENER_ON_AFTER_CLASS,
        IEXECUTION_VISUALISER_CONSUME_DOT_DEFINITION,
        ITESTLISTENER_ON_FINISH_TEST_TAG,
        ISUITELISTENER_ON_FINISH,
        IREPORTER_GENERATE_REPORT,
        IEXECUTIONLISTENER_ON_EXECUTION_FINISH
    );
    runTest(expected, SimpleTestClassWithFailedConfigAndSkippedTestMethod.class);
  }

  @Test(description = "Test class has only 1 failed config, 1 skipped config and skipped test method")
  public void testOrderHasOnlyFailedAndSkippedConfigAndSkippedTestMethod() {
    List<String> expected = Arrays.asList(
        IEXECUTIONLISTENER_ON_EXECUTION_START,
        IALTERSUITELISTENER_ALTER,
        IANNOTATIONTRANSFORMER_TRANSFORM_3_ARGS,
        ISUITELISTENER_ON_START,
        ITESTLISTENER_ON_START_TEST_TAG,
        METHODINTERCEPTOR_INTERCEPT,
        METHODINTERCEPTOR_INTERCEPT,
        ICLASSLISTENER_ON_BEFORE_CLASS,
        ICONFIGURATIONLISTENER_BEFORE_CONFIGURATION,
        IINVOKEDMETHODLISTENER_BEFORE_INVOCATION,
        IINVOKEDMETHODLISTENER_AFTER_INVOCATION,
        ICONFIGURATIONLISTENER_ON_CONFIGURATION_FAILURE,
        IINVOKEDMETHODLISTENER_BEFORE_INVOCATION,
        IINVOKEDMETHODLISTENER_AFTER_INVOCATION,
        ICONFIGURATIONLISTENER_ON_CONFIGURATION_SKIP,
        ITESTLISTENER_ON_START_TEST_METHOD,
        IINVOKEDMETHODLISTENER_BEFORE_INVOCATION,
        IINVOKEDMETHODLISTENER_AFTER_INVOCATION,
        ITESTLISTENER_ON_TEST_SKIPPED_TEST_METHOD,
        ICLASSLISTENER_ON_AFTER_CLASS,
        IEXECUTION_VISUALISER_CONSUME_DOT_DEFINITION,
        ITESTLISTENER_ON_FINISH_TEST_TAG,
        ISUITELISTENER_ON_FINISH,
        IREPORTER_GENERATE_REPORT,
        IEXECUTIONLISTENER_ON_EXECUTION_FINISH
    );
    runTest(expected, SimpleTestClassWithFailedAndSkippedConfigAndSkippedTestMethod.class);
  }

  //All Data driven scenarios

  @Test(description = "Test class has only 1 passed test method powered by a data provider")
  public void testOrderHasOnlyDataDrivenPassedMethod() {
    List<String> expected = Arrays.asList(
        IEXECUTIONLISTENER_ON_EXECUTION_START,
        IALTERSUITELISTENER_ALTER,
        IANNOTATIONTRANSFORMER_TRANSFORM_3_ARGS,
        ISUITELISTENER_ON_START,
        ITESTLISTENER_ON_START_TEST_TAG,
        METHODINTERCEPTOR_INTERCEPT,
        METHODINTERCEPTOR_INTERCEPT,
        ICLASSLISTENER_ON_BEFORE_CLASS,
        IDATAPROVIDERLISTENER_BEFORE_DATA_PROVIDER_EXECUTION,
        IDATAPROVIDERLISTENER_AFTER_DATA_PROVIDER_EXECUTION,
        ITESTLISTENER_ON_START_TEST_METHOD,
        IINVOKEDMETHODLISTENER_BEFORE_INVOCATION,
        IINVOKEDMETHODLISTENER_AFTER_INVOCATION,
        ITESTLISTENER_ON_TEST_SUCCESS_TEST_METHOD,
        ITESTLISTENER_ON_START_TEST_METHOD,
        IINVOKEDMETHODLISTENER_BEFORE_INVOCATION,
        IINVOKEDMETHODLISTENER_AFTER_INVOCATION,
        ITESTLISTENER_ON_TEST_SUCCESS_TEST_METHOD,
        ICLASSLISTENER_ON_AFTER_CLASS,
        IEXECUTION_VISUALISER_CONSUME_DOT_DEFINITION,
        ITESTLISTENER_ON_FINISH_TEST_TAG,
        ISUITELISTENER_ON_FINISH,
        IREPORTER_GENERATE_REPORT,
        IEXECUTIONLISTENER_ON_EXECUTION_FINISH
    );
    runTest(expected, SimpleTestClassWithDataDrivenPassMethod.class);
  }

  @Test(description = "Test class has only 1 data provider driven test method with 1 passed and 1 failed iteration.")
  public void testOrderHasOnlyDataDrivenMethodWithPassedAndFailedIteration() {
    List<String> expected = Arrays.asList(
        IEXECUTIONLISTENER_ON_EXECUTION_START,
        IALTERSUITELISTENER_ALTER,
        IANNOTATIONTRANSFORMER_TRANSFORM_3_ARGS,
        ISUITELISTENER_ON_START,
        ITESTLISTENER_ON_START_TEST_TAG,
        METHODINTERCEPTOR_INTERCEPT,
        METHODINTERCEPTOR_INTERCEPT,
        ICLASSLISTENER_ON_BEFORE_CLASS,
        IDATAPROVIDERLISTENER_BEFORE_DATA_PROVIDER_EXECUTION,
        IDATAPROVIDERLISTENER_AFTER_DATA_PROVIDER_EXECUTION,
        ITESTLISTENER_ON_START_TEST_METHOD,
        IINVOKEDMETHODLISTENER_BEFORE_INVOCATION,
        IINVOKEDMETHODLISTENER_AFTER_INVOCATION,
        ITESTLISTENER_ON_TEST_FAILURE_TEST_METHOD,
        ITESTLISTENER_ON_START_TEST_METHOD,
        IINVOKEDMETHODLISTENER_BEFORE_INVOCATION,
        IINVOKEDMETHODLISTENER_AFTER_INVOCATION,
        ITESTLISTENER_ON_TEST_SUCCESS_TEST_METHOD,
        ICLASSLISTENER_ON_AFTER_CLASS,
        IEXECUTION_VISUALISER_CONSUME_DOT_DEFINITION,
        ITESTLISTENER_ON_FINISH_TEST_TAG,
        ISUITELISTENER_ON_FINISH,
        IREPORTER_GENERATE_REPORT,
        IEXECUTIONLISTENER_ON_EXECUTION_FINISH
    );
    runTest(expected, SimpleTestClassWithDataDrivenMethodPassAndFailedIterations.class);
  }

  //Regular test methods without any configuration methods.

  @Test(description = "Test class has only 1 passed test method")
  public void testOrderHasOnlyPassedMethod() {
    List<String> expected = Arrays.asList(
        IEXECUTIONLISTENER_ON_EXECUTION_START,
        IALTERSUITELISTENER_ALTER,
        IANNOTATIONTRANSFORMER_TRANSFORM_3_ARGS,
        ISUITELISTENER_ON_START,
        ITESTLISTENER_ON_START_TEST_TAG,
        METHODINTERCEPTOR_INTERCEPT,
        METHODINTERCEPTOR_INTERCEPT,
        ICLASSLISTENER_ON_BEFORE_CLASS,
        ITESTLISTENER_ON_START_TEST_METHOD,
        IINVOKEDMETHODLISTENER_BEFORE_INVOCATION,
        IINVOKEDMETHODLISTENER_AFTER_INVOCATION,
        ITESTLISTENER_ON_TEST_SUCCESS_TEST_METHOD,
        ICLASSLISTENER_ON_AFTER_CLASS,
        IEXECUTION_VISUALISER_CONSUME_DOT_DEFINITION,
        ITESTLISTENER_ON_FINISH_TEST_TAG,
        ISUITELISTENER_ON_FINISH,
        IREPORTER_GENERATE_REPORT,
        IEXECUTIONLISTENER_ON_EXECUTION_FINISH
    );
    runTest(expected, SimpleTestClassWithPassMethod.class);
  }

  @Test(description = "Test class has only 1 failed test method")
  public void testOrderHasOnlyFailedMethod() {
    List<String> expected = Arrays.asList(
        IEXECUTIONLISTENER_ON_EXECUTION_START,
        IALTERSUITELISTENER_ALTER,
        IANNOTATIONTRANSFORMER_TRANSFORM_3_ARGS,
        ISUITELISTENER_ON_START,
        ITESTLISTENER_ON_START_TEST_TAG,
        METHODINTERCEPTOR_INTERCEPT,
        METHODINTERCEPTOR_INTERCEPT,
        ICLASSLISTENER_ON_BEFORE_CLASS,
        ITESTLISTENER_ON_START_TEST_METHOD,
        IINVOKEDMETHODLISTENER_BEFORE_INVOCATION,
        IINVOKEDMETHODLISTENER_AFTER_INVOCATION,
        ITESTLISTENER_ON_TEST_FAILURE_TEST_METHOD,
        ICLASSLISTENER_ON_AFTER_CLASS,
        IEXECUTION_VISUALISER_CONSUME_DOT_DEFINITION,
        ITESTLISTENER_ON_FINISH_TEST_TAG,
        ISUITELISTENER_ON_FINISH,
        IREPORTER_GENERATE_REPORT,
        IEXECUTIONLISTENER_ON_EXECUTION_FINISH
    );
    runTest(expected, SimpleTestClassWithFailedMethod.class);
  }

  @Test(description = "Test class has only 1 failed test method and uses invocation counts")
  public void testOrderHasOnlyFailedMethodMultipleInvocations() {
    List<String> expected = Arrays.asList(
        IEXECUTIONLISTENER_ON_EXECUTION_START,
        IALTERSUITELISTENER_ALTER,
        IANNOTATIONTRANSFORMER_TRANSFORM_3_ARGS,
        ISUITELISTENER_ON_START,
        ITESTLISTENER_ON_START_TEST_TAG,
        METHODINTERCEPTOR_INTERCEPT,
        METHODINTERCEPTOR_INTERCEPT,
        ICLASSLISTENER_ON_BEFORE_CLASS,
        ITESTLISTENER_ON_START_TEST_METHOD,
        IINVOKEDMETHODLISTENER_BEFORE_INVOCATION,
        IINVOKEDMETHODLISTENER_AFTER_INVOCATION,
        ITESTLISTENER_ON_TEST_FAILURE_TEST_METHOD,
        ITESTLISTENER_ON_START_TEST_METHOD,
        IINVOKEDMETHODLISTENER_BEFORE_INVOCATION,
        IINVOKEDMETHODLISTENER_AFTER_INVOCATION,
        ITESTLISTENER_ON_TEST_FAILURE_TEST_METHOD,
        ICLASSLISTENER_ON_AFTER_CLASS,
        IEXECUTION_VISUALISER_CONSUME_DOT_DEFINITION,
        ITESTLISTENER_ON_FINISH_TEST_TAG,
        ISUITELISTENER_ON_FINISH,
        IREPORTER_GENERATE_REPORT,
        IEXECUTIONLISTENER_ON_EXECUTION_FINISH
    );
    runTest(expected, SimpleTestClassWithFailedMethodMultipleInvocations.class);
  }

  @Test(description = "Test class has only 1 failed test method and uses invocation counts but configured"
      + "to skip failed invocations")
  public void testOrderHasOnlyFailedMethodMultipleInvocationsAndSkipFailedInvocations() {
    List<String> expected = Arrays.asList(
        IEXECUTIONLISTENER_ON_EXECUTION_START,
        IALTERSUITELISTENER_ALTER,
        IANNOTATIONTRANSFORMER_TRANSFORM_3_ARGS,
        ISUITELISTENER_ON_START,
        ITESTLISTENER_ON_START_TEST_TAG,
        METHODINTERCEPTOR_INTERCEPT,
        METHODINTERCEPTOR_INTERCEPT,
        ICLASSLISTENER_ON_BEFORE_CLASS,
        ITESTLISTENER_ON_START_TEST_METHOD,
        IINVOKEDMETHODLISTENER_BEFORE_INVOCATION,
        IINVOKEDMETHODLISTENER_AFTER_INVOCATION,
        ITESTLISTENER_ON_TEST_FAILURE_TEST_METHOD,
        ITESTLISTENER_ON_START_TEST_METHOD,
        IINVOKEDMETHODLISTENER_BEFORE_INVOCATION,
        IINVOKEDMETHODLISTENER_AFTER_INVOCATION,
        ITESTLISTENER_ON_TEST_SKIPPED_TEST_METHOD,
        ICLASSLISTENER_ON_AFTER_CLASS,
        IEXECUTION_VISUALISER_CONSUME_DOT_DEFINITION,
        ITESTLISTENER_ON_FINISH_TEST_TAG,
        ISUITELISTENER_ON_FINISH,
        IREPORTER_GENERATE_REPORT,
        IEXECUTIONLISTENER_ON_EXECUTION_FINISH
    );
    runTest(expected, SimpleTestClassWithFailedMethodMultipleInvocations.class, true);
  }

  @Test(description = "Test class has passed/failed/skipped test methods")
  public void testOrderHasPassedFailedSkippedMethods() {
    List<String> expected = Arrays.asList(
        IEXECUTIONLISTENER_ON_EXECUTION_START,
        IALTERSUITELISTENER_ALTER,
        IANNOTATIONTRANSFORMER_TRANSFORM_3_ARGS,
        ISUITELISTENER_ON_START,
        ITESTLISTENER_ON_START_TEST_TAG,
        METHODINTERCEPTOR_INTERCEPT,
        METHODINTERCEPTOR_INTERCEPT,
        ICLASSLISTENER_ON_BEFORE_CLASS,
        ITESTLISTENER_ON_START_TEST_METHOD,
        IINVOKEDMETHODLISTENER_BEFORE_INVOCATION,
        IINVOKEDMETHODLISTENER_AFTER_INVOCATION,
        ITESTLISTENER_ON_TEST_FAILURE_TEST_METHOD,
        IEXECUTION_VISUALISER_CONSUME_DOT_DEFINITION,
        ITESTLISTENER_ON_START_TEST_METHOD,
        IINVOKEDMETHODLISTENER_BEFORE_INVOCATION,
        IINVOKEDMETHODLISTENER_AFTER_INVOCATION,
        ITESTLISTENER_ON_TEST_SUCCESS_TEST_METHOD,
        IEXECUTION_VISUALISER_CONSUME_DOT_DEFINITION,
        ITESTLISTENER_ON_START_TEST_METHOD,
        IINVOKEDMETHODLISTENER_BEFORE_INVOCATION,
        IINVOKEDMETHODLISTENER_AFTER_INVOCATION,
        ITESTLISTENER_ON_TEST_SKIPPED_TEST_METHOD,
        ICLASSLISTENER_ON_AFTER_CLASS,
        IEXECUTION_VISUALISER_CONSUME_DOT_DEFINITION,
        ITESTLISTENER_ON_FINISH_TEST_TAG,
        ISUITELISTENER_ON_FINISH,
        IREPORTER_GENERATE_REPORT,
        IEXECUTIONLISTENER_ON_EXECUTION_FINISH
    );
    runTest(expected, SimpleTestClassWithPassFailMethods.class);
  }

  @Test(description = "Test class has only 1 failed test method and is powered by factory")
  public void testOrderHasOnlyFailedMethodPoweredByFactory() {
    List<String> expected = Arrays.asList(
        IEXECUTIONLISTENER_ON_EXECUTION_START,
        IALTERSUITELISTENER_ALTER,
        IANNOTATIONTRANSFORMER_TRANSFORM_3_ARGS,
        ISUITELISTENER_ON_START,
        ITESTLISTENER_ON_START_TEST_TAG,
        METHODINTERCEPTOR_INTERCEPT,
        METHODINTERCEPTOR_INTERCEPT,
        ICLASSLISTENER_ON_BEFORE_CLASS,
        ITESTLISTENER_ON_START_TEST_METHOD,
        IINVOKEDMETHODLISTENER_BEFORE_INVOCATION,
        IINVOKEDMETHODLISTENER_AFTER_INVOCATION,
        ITESTLISTENER_ON_TEST_FAILURE_TEST_METHOD,
        ICLASSLISTENER_ON_AFTER_CLASS,
        IEXECUTION_VISUALISER_CONSUME_DOT_DEFINITION,

        ICLASSLISTENER_ON_BEFORE_CLASS,
        ITESTLISTENER_ON_START_TEST_METHOD,
        IINVOKEDMETHODLISTENER_BEFORE_INVOCATION,
        IINVOKEDMETHODLISTENER_AFTER_INVOCATION,
        ITESTLISTENER_ON_TEST_FAILURE_TEST_METHOD,
        ICLASSLISTENER_ON_AFTER_CLASS,
        IEXECUTION_VISUALISER_CONSUME_DOT_DEFINITION,
        ITESTLISTENER_ON_FINISH_TEST_TAG,
        ISUITELISTENER_ON_FINISH,
        IREPORTER_GENERATE_REPORT,
        IEXECUTIONLISTENER_ON_EXECUTION_FINISH
    );
    runTest(expected, SimpleTestClassPoweredByFactoryWithFailedMethod.class);
  }

  @Test(description = "Test class has only 1 failed test method with retry analyzer")
  public void testOrderHasOnlyFailedMethodWithRetryMechanism() {
    List<String> expected = Arrays.asList(
        IEXECUTIONLISTENER_ON_EXECUTION_START,
        IALTERSUITELISTENER_ALTER,
        IANNOTATIONTRANSFORMER_TRANSFORM_3_ARGS,
        ISUITELISTENER_ON_START,
        ITESTLISTENER_ON_START_TEST_TAG,
        METHODINTERCEPTOR_INTERCEPT,
        METHODINTERCEPTOR_INTERCEPT,
        ICLASSLISTENER_ON_BEFORE_CLASS,
        ITESTLISTENER_ON_START_TEST_METHOD,
        IINVOKEDMETHODLISTENER_BEFORE_INVOCATION,
        IINVOKEDMETHODLISTENER_AFTER_INVOCATION,
        ITESTLISTENER_ON_TEST_SKIPPED_TEST_METHOD,
        ITESTLISTENER_ON_START_TEST_METHOD,
        IINVOKEDMETHODLISTENER_BEFORE_INVOCATION,
        IINVOKEDMETHODLISTENER_AFTER_INVOCATION,
        ITESTLISTENER_ON_TEST_FAILURE_TEST_METHOD,
        ICLASSLISTENER_ON_AFTER_CLASS,
        IEXECUTION_VISUALISER_CONSUME_DOT_DEFINITION,
        ITESTLISTENER_ON_FINISH_TEST_TAG,
        ISUITELISTENER_ON_FINISH,
        IREPORTER_GENERATE_REPORT,
        IEXECUTIONLISTENER_ON_EXECUTION_FINISH
    );
    runTest(expected, SimpleTestClassWithFailedMethodHasRetryAnalyzer.class, true);
  }

  private static void runTest(List<String> expected, Class<?> clazz) {
    runTest(expected, clazz, false);
  }

  private static void runTest(List<String> expected, Class<?> clazz, boolean skipInvocationCount) {
    TestNG testng = create(clazz);
    testng.setSkipFailedInvocationCounts(skipInvocationCount);
    UniversalListener listener = new UniversalListener();
    testng.addListener(listener);
    testng.alwaysRunListeners(true);
    testng.run();
    Assertions.assertThat(listener.getMessages()).containsExactlyElementsOf(expected);
  }

}
