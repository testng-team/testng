package test.factory.lazy;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Lazy;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

/**
 * Vets every combination of the three lazy opt-in levers — TestNG configuration, suite XML
 * attribute and {@code @Factory} annotation — across all three states each can hold, expressed with
 * the same {@link Lazy} enum the feature itself uses:
 *
 * <ul>
 *   <li>{@link Lazy#TRUE} — the level turns lazy on.
 *   <li>{@link Lazy#FALSE} — the level turns lazy off.
 *   <li>{@link Lazy#UNSET} — the level says nothing, deferring to the broader levels.
 * </ul>
 *
 * <p>Resolution follows the precedence <b>annotation &gt; suite &gt; configuration &gt; eager
 * default</b>: the most granular level that is not {@link Lazy#UNSET} decides; if all are {@link
 * Lazy#UNSET} the default (eager) applies. Note the configuration level has no distinct "disabled"
 * behavior — it is a plain boolean defaulting to {@code false}, so {@link Lazy#UNSET} and {@link
 * Lazy#FALSE} resolve identically there (both rows are included to make that explicit).
 */
public class LazyFactoryResolutionMatrixTest extends SimpleBaseTest {

  @DataProvider(name = "combinations")
  public Object[][] combinations() {
    // config, suite, annotation, expected-lazy
    return new Object[][] {
      // --- annotation TRUE: always lazy, regardless of suite/config ---
      {Lazy.UNSET, Lazy.UNSET, Lazy.TRUE, true},
      {Lazy.FALSE, Lazy.UNSET, Lazy.TRUE, true},
      {Lazy.TRUE, Lazy.UNSET, Lazy.TRUE, true},
      {Lazy.UNSET, Lazy.FALSE, Lazy.TRUE, true},
      {Lazy.FALSE, Lazy.FALSE, Lazy.TRUE, true},
      {Lazy.TRUE, Lazy.FALSE, Lazy.TRUE, true},
      {Lazy.UNSET, Lazy.TRUE, Lazy.TRUE, true},
      {Lazy.FALSE, Lazy.TRUE, Lazy.TRUE, true},
      {Lazy.TRUE, Lazy.TRUE, Lazy.TRUE, true},

      // --- annotation FALSE: always eager, regardless of suite/config ---
      {Lazy.UNSET, Lazy.UNSET, Lazy.FALSE, false},
      {Lazy.FALSE, Lazy.UNSET, Lazy.FALSE, false},
      {Lazy.TRUE, Lazy.UNSET, Lazy.FALSE, false},
      {Lazy.UNSET, Lazy.FALSE, Lazy.FALSE, false},
      {Lazy.FALSE, Lazy.FALSE, Lazy.FALSE, false},
      {Lazy.TRUE, Lazy.FALSE, Lazy.FALSE, false},
      {Lazy.UNSET, Lazy.TRUE, Lazy.FALSE, false},
      {Lazy.FALSE, Lazy.TRUE, Lazy.FALSE, false},
      {Lazy.TRUE, Lazy.TRUE, Lazy.FALSE, false},

      // --- annotation UNSET: suite decides; if suite UNSET, configuration decides ---
      // suite TRUE -> lazy (config irrelevant)
      {Lazy.UNSET, Lazy.TRUE, Lazy.UNSET, true},
      {Lazy.FALSE, Lazy.TRUE, Lazy.UNSET, true},
      {Lazy.TRUE, Lazy.TRUE, Lazy.UNSET, true},
      // suite FALSE -> eager (config irrelevant; this is the suite-vetoes-config case)
      {Lazy.UNSET, Lazy.FALSE, Lazy.UNSET, false},
      {Lazy.FALSE, Lazy.FALSE, Lazy.UNSET, false},
      {Lazy.TRUE, Lazy.FALSE, Lazy.UNSET, false},
      // suite UNSET -> configuration decides (FALSE == UNSET == eager; only TRUE -> lazy)
      {Lazy.UNSET, Lazy.UNSET, Lazy.UNSET, false},
      {Lazy.FALSE, Lazy.UNSET, Lazy.UNSET, false},
      {Lazy.TRUE, Lazy.UNSET, Lazy.UNSET, true},
    };
  }

  @Test(dataProvider = "combinations")
  public void resolves(Lazy config, Lazy suite, Lazy annotation, boolean expectedLazy) {
    // The annotation lever is expressed by choosing the sample whose @Factory carries that value.
    Class<?> sample = sampleFor(annotation);
    reset(annotation);

    XmlSuite xmlSuite = createXmlSuite("lazy-matrix");
    applySuite(xmlSuite, suite);
    XmlTest xmlTest = createXmlTest(xmlSuite, "t");
    createXmlClass(xmlTest, sample);

    TestNG tng = create(xmlSuite);
    tng.setPreserveOrder(true);
    applyConfig(tng, config);
    tng.run();

    List<Integer> alive = aliveWhenEachTestRan(annotation);
    assertThat(alive)
        .as(
            "every instance's test must run (config=%s, suite=%s, annotation=%s)",
            config, suite, annotation)
        .hasSize(4);
    assertThat(wasLazy(alive))
        .as("config=%s, suite=%s, annotation=%s", config, suite, annotation)
        .isEqualTo(expectedLazy);
  }

  private static void applyConfig(TestNG tng, Lazy config) {
    switch (config) {
      case TRUE:
        tng.setLazyFactoryInstantiation(true);
        break;
      case FALSE:
        tng.setLazyFactoryInstantiation(false);
        break;
      case UNSET:
      default:
        // leave the configuration untouched (defaults to eager)
    }
  }

  private static void applySuite(XmlSuite xmlSuite, Lazy suite) {
    switch (suite) {
      case TRUE:
        xmlSuite.setLazyFactory(true);
        break;
      case FALSE:
        xmlSuite.setLazyFactory(false);
        break;
      case UNSET:
      default:
        // leave the suite attribute unset (null)
    }
  }

  private static Class<?> sampleFor(Lazy annotation) {
    switch (annotation) {
      case TRUE:
        return AnnotationLazyTrueSample.class;
      case FALSE:
        return AnnotationLazyFalseSample.class;
      case UNSET:
      default:
        return CountingFactorySample.class;
    }
  }

  private static void reset(Lazy annotation) {
    switch (annotation) {
      case TRUE:
        AnnotationLazyTrueSample.reset();
        break;
      case FALSE:
        AnnotationLazyFalseSample.reset();
        break;
      case UNSET:
      default:
        CountingFactorySample.reset();
    }
  }

  private static List<Integer> aliveWhenEachTestRan(Lazy annotation) {
    switch (annotation) {
      case TRUE:
        return AnnotationLazyTrueSample.INSTANCES_ALIVE_WHEN_EACH_TEST_RAN;
      case FALSE:
        return AnnotationLazyFalseSample.INSTANCES_ALIVE_WHEN_EACH_TEST_RAN;
      case UNSET:
      default:
        return CountingFactorySample.INSTANCES_ALIVE_WHEN_EACH_TEST_RAN;
    }
  }

  /**
   * @return - {@code true} when the run was lazy: the first test ran before every instance had been
   *     constructed (so fewer than all instances were alive at that point). Eager runs have all
   *     instances alive from the very first test, so this is {@code false}.
   */
  private static boolean wasLazy(List<Integer> aliveWhenEachTestRan) {
    return aliveWhenEachTestRan.get(0) < aliveWhenEachTestRan.size();
  }
}
