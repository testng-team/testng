package test.factory.issue3111;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import org.testng.IFactory;
import org.testng.IFactoryInstance;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;
import test.factory.lazy.CountingFactorySample;

/**
 * {@code ITestResult.getFactoryInstance()} -- the replacement for the deprecated {@code
 * IClass.getInstanceHashCodes()} / {@code getInstances(boolean)} pair that {@code testng-engine}
 * used to identify the executions a {@code @Factory} produces.
 *
 * <p>Everything here goes through the public API, on purpose: the acceptance criterion for #3111 is
 * that an external consumer can write {@code result.getFactoryInstance().map(IFactoryInstance::
 * getIndex)} without importing anything from {@code org.testng.internal}.
 */
public class FactoryInstanceTest extends SimpleBaseTest {

  @Test(description = "GITHUB-3111")
  public void constructorFactoryIndexesEachRowOfItsDataProvider() {
    List<IFactoryInstance> instances = run(SimpleFactoryPoweredTestSample.class);

    assertThat(indexesOf(instances)).containsExactly(0, 1, 2);
    assertThat(parametersOf(instances)).containsExactly("[1]", "[2]", "[3]");
  }

  @Test(description = "GITHUB-3111")
  public void factoryMethodIndexesEveryInstanceItReturned() {
    // The regression #3111 was opened for: a factory method returning several instances from one
    // invocation used to give all of them the same index -- 0, with no data provider to move it.
    List<IFactoryInstance> instances = run(SimpleFactoryPoweredTestWithoutDataProviderSample.class);

    assertThat(indexesOf(instances)).containsExactly(0, 1, 2);
  }

  @Test(description = "GITHUB-3111")
  public void indicesSelectWithoutRenumbering() {
    // The index is the position in the factory's output, taken before the indices attribute filters
    // it -- so the surviving instance keeps the number that selected it.
    assertThat(indexesOf(run(SimpleFactoryPoweredTestWithIndicesSample.class))).containsExactly(1);
    assertThat(indexesOf(run(SimpleFactoryPoweredTestWithoutDataProviderWithIndicesSample.class)))
        .containsExactly(1);
  }

  @Test(description = "GITHUB-3111")
  public void instancesOfOneInvocationAreNumberedApartButShareItsParameters() {
    List<IFactoryInstance> instances = run(MultiInstancePerRowFactorySample.class);

    assertThat(indexesOf(instances)).containsExactly(0, 1, 2, 3);
    // Two instances per row, so the row's parameters are what both of them report.
    assertThat(parametersOf(instances)).containsExactly("[a]", "[a]", "[b]", "[b]");
  }

  @Test(description = "GITHUB-3111")
  public void theFactoryIsDescribedWithoutLeakingItsImplementation() {
    IFactory constructorFactory =
        byIndex(run(SimpleFactoryPoweredTestSample.class), 0).getFactory();
    assertThat(constructorFactory.getDeclaringClass())
        .isEqualTo(SimpleFactoryPoweredTestSample.class);
    // A constructor reports its name as the declaring class's fully qualified name.
    assertThat(constructorFactory.getName())
        .isEqualTo(SimpleFactoryPoweredTestSample.class.getName());
    assertThat(constructorFactory.isLazy()).isFalse();

    IFactory methodFactory =
        byIndex(run(SimpleFactoryPoweredTestWithoutDataProviderSample.class), 0).getFactory();
    assertThat(methodFactory.getName()).isEqualTo("data");
    assertThat(methodFactory.getDeclaringClass())
        .isEqualTo(SimpleFactoryPoweredTestWithoutDataProviderSample.class);
  }

  @Test(description = "GITHUB-3111")
  public void aTestClassNoFactoryProducedHasNoFactoryInstance() {
    assertThat(run(NonFactorySample.class)).isEmpty();
  }

  @Test(description = "GITHUB-3111")
  public void parametersAreCopiedSoACallerCannotCorruptThem() {
    IFactoryInstance instance = byIndex(run(SimpleFactoryPoweredTestSample.class), 0);

    Object[] first = instance.getParameters();
    first[0] = "tampered";

    assertThat(instance.getParameters()).containsExactly(1);
  }

  @Test(description = "GITHUB-3111")
  public void everyParallelInstanceIsAccountedForExactlyOnce() {
    TestNG tng = create(ParallelFactorySample.class);
    tng.setParallel(XmlSuite.ParallelMode.INSTANCES);
    tng.setThreadCount(4);
    CollectingListener listener = new CollectingListener();
    tng.addListener(listener);
    tng.run();

    assertThat(indexesOf(listener.instances)).containsExactly(0, 1, 2, 3);
  }

  @Test(description = "GITHUB-3111")
  public void readingTheMetadataOfALazyInstanceDoesNotCreateIt() {
    // An IMethodInterceptor runs before any instance is built, which is the moment that matters:
    // if reading the factory metadata instantiated, laziness would be lost for the whole run.
    CountingFactorySample.reset();
    List<Integer> seenIndexes = new ArrayList<>();
    List<Integer> constructedWhileReading = new ArrayList<>();

    TestNG tng = create(CountingFactorySample.class);
    tng.setPreserveOrder(true);
    tng.setLazyFactoryInstantiation(true);
    tng.addListener(
        (IMethodInterceptor)
            (methods, context) -> {
              for (IMethodInstance mi : methods) {
                mi.getMethod()
                    .getFactoryInstance()
                    .ifPresent(
                        it -> {
                          seenIndexes.add(it.getIndex());
                          it.getParameters();
                          it.getFactory().isLazy();
                        });
              }
              constructedWhileReading.add(CountingFactorySample.CONSTRUCTED.get());
              return methods;
            });
    tng.run();

    assertThat(seenIndexes)
        .as("every instance was described before any existed")
        .containsExactly(0, 1, 2, 3);
    assertThat(constructedWhileReading)
        .as("reading index, parameters and factory instantiated nothing")
        .containsExactly(0);
    assertThat(CountingFactorySample.INSTANCES_ALIVE_WHEN_EACH_TEST_RAN)
        .as("laziness survived the inspection")
        .containsExactly(1, 2, 3, 4);
  }

  @Test(description = "GITHUB-3111")
  public void aLazyFactoryReportsItselfAsLazy() {
    CountingFactorySample.reset();
    TestNG tng = create(CountingFactorySample.class);
    tng.setLazyFactoryInstantiation(true);
    CollectingListener listener = new CollectingListener();
    tng.addListener(listener);
    tng.run();

    assertThat(listener.instances).isNotEmpty();
    assertThat(listener.instances).allMatch(it -> it.getFactory().isLazy());
  }

  @Test(description = "GITHUB-3111")
  @SuppressWarnings("deprecation")
  public void theDeprecatedIndexKeepsMeaningTheInvocation() {
    // Guard: the new index is additive. IParameterInfo.getIndex() still answers "which invocation",
    // which for a factory method returning three instances at once is 0 for all of them.
    List<Integer> legacy = new ArrayList<>();
    TestNG tng = create(SimpleFactoryPoweredTestWithoutDataProviderSample.class);
    tng.addListener(
        new ITestListener() {
          @Override
          public void onTestSuccess(ITestResult result) {
            legacy.add(result.getMethod().getFactoryMethodParamsInfo().getIndex());
          }
        });
    tng.run();

    assertThat(legacy).containsExactly(0, 0, 0);
  }

  /** Results arrive in completion order, so pick the instance by the index it reports. */
  private static IFactoryInstance byIndex(List<IFactoryInstance> instances, int index) {
    return instances.stream()
        .filter(it -> it.getIndex() == index)
        .findFirst()
        .orElseThrow(() -> new AssertionError("no factory instance with index " + index));
  }

  private static List<Integer> indexesOf(List<IFactoryInstance> instances) {
    return instances.stream().map(IFactoryInstance::getIndex).sorted().collect(Collectors.toList());
  }

  private static List<String> parametersOf(List<IFactoryInstance> instances) {
    return instances.stream()
        .sorted(java.util.Comparator.comparingInt(IFactoryInstance::getIndex))
        .map(it -> Arrays.toString(it.getParameters()))
        .collect(Collectors.toList());
  }

  private static List<IFactoryInstance> run(Class<?> sample) {
    TestNG tng = create(sample);
    CollectingListener listener = new CollectingListener();
    tng.addListener(listener);
    tng.run();
    return listener.instances;
  }

  private static class CollectingListener implements ITestListener {
    // onTestSuccess is called from every worker thread when the suite runs in parallel.
    private final List<IFactoryInstance> instances = new CopyOnWriteArrayList<>();

    @Override
    public void onTestSuccess(ITestResult result) {
      result.getFactoryInstance().ifPresent(instances::add);
    }
  }
}
