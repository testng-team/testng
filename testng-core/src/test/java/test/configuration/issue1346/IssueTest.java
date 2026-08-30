package test.configuration.issue1346;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

/**
 * Pins where the group level configuration sits in the lifecycle, and why GITHUB-1346 -- which asks
 * for {@code @BeforeTest -> @BeforeGroups -> @BeforeClass} and {@code @AfterClass -> @AfterGroups}
 * -- describes an order TestNG cannot state as a contract.
 *
 * <p>A group's configuration is pulled on first encounter of one of its test methods, from inside
 * the same per-class loop that pulls {@code @BeforeClass} and {@code @AfterClass}. So the class
 * boundary encloses the group boundary.
 *
 * <p>The requested order presumes that a group contains classes. It does not: a group is a
 * cross-cutting selector over methods, so it need not nest inside or outside a class at all. Two
 * shapes below settle it -- {@link #twoGroupsInterleavedInsideOneClass()}, where the class enters a
 * second group after its own {@code @BeforeClass} has run and leaves the first one before its
 * {@code @AfterClass}, and {@link #oneMethodBelongingToTwoGroups()}, where neither group is the
 * enclosing one. Honouring GITHUB-1346 would need the scheduler to run every group's methods
 * contiguously, which is a different feature from a lifecycle reordering.
 */
public class IssueTest extends SimpleBaseTest {

  @Test(description = "GITHUB-1346")
  public void groupConfigurationIsInvokedInsideTheClassLifecycle() {
    InvokedMethodNameListener listener = run(LifecycleOrderSample.class);

    // This is the sequence GITHUB-1346 proposes to invert. It does not: see the class javadoc.
    assertThat(listener.getInvokedMethodNames())
        .containsExactly(
            "beforeSuite",
            "beforeTest",
            "beforeClass",
            "beforeGroups",
            "beforeMethod",
            "test",
            "afterMethod",
            "afterGroups",
            "afterClass",
            "afterTest",
            "afterSuite");
  }

  @Test(description = "GITHUB-1346")
  public void twoGroupsInterleavedInsideOneClass() {
    InvokedMethodNameListener listener = run(InterleavedGroupsSample.class);

    // beforeGroupsTwo is pulled by test2, which is the fourth method to be invoked -- there is no
    // ordering in which it precedes the single @BeforeClass of the class it is declared on. Nor can
    // afterGroupsOne follow @AfterClass: test4 of the same class is still to come when group g1 is
    // left.
    assertThat(listener.getInvokedMethodNames())
        .containsExactly(
            "beforeClass",
            "beforeGroupsOne",
            "test1",
            "beforeGroupsTwo",
            "test2",
            "test3",
            "afterGroupsOne",
            "test4",
            "afterGroupsTwo",
            "afterClass");
  }

  @Test(description = "GITHUB-1346")
  public void oneMethodBelongingToTwoGroups() {
    InvokedMethodNameListener listener = run(TwoGroupsPerMethodSample.class);

    // test1 belongs to both groups, so both group setups are due before it and neither group is the
    // enclosing scope of the other. g2 is left as soon as test1 is over, in the middle of the
    // class.
    assertThat(listener.getInvokedMethodNames())
        .containsExactly(
            "beforeClass",
            "beforeGroupsOne",
            "beforeGroupsTwo",
            "test1",
            "afterGroupsTwo",
            "test2",
            "afterGroupsOne",
            "afterClass");
  }

  @Test(description = "GITHUB-1346")
  public void aGroupSpanningSeveralClasses() {
    InvokedMethodNameListener listener =
        run(MultiClassGroupAlphaSample.class, MultiClassGroupBravoSample.class);

    // The shape GITHUB-1346 argues from -- one group over two classes -- and even here the group
    // configuration sits inside a class lifecycle at both ends: it is entered after the first
    // class's @BeforeClass and left before the last class's @AfterClass.
    assertThat(listener.getInvokedMethodNames())
        .containsExactly(
            "alphaBeforeClass",
            "beforeGroups",
            "alphaTest",
            "alphaAfterClass",
            "bravoBeforeClass",
            "bravoTest",
            "afterGroups",
            "bravoAfterClass");
  }

  @Test(description = "GITHUB-1346")
  public void aGroupThatIsNotSelectedRunsNoConfiguration() {
    XmlSuite suite = createXmlSuite("1346-excluded");
    XmlTest test = createXmlTest(suite, "excluded", InterleavedGroupsSample.class);
    createXmlGroups(test, "g1");

    InvokedMethodNameListener listener = run(suite);

    // Only g1 is selected, so the g2 configuration never runs even though it is declared on a class
    // that does take part in the run.
    assertThat(listener.getInvokedMethodNames())
        .containsExactly(
            "beforeClass", "beforeGroupsOne", "test1", "test3", "afterGroupsOne", "afterClass");
  }

  @Test(description = "GITHUB-1346")
  public void severalInstancesOfOneClassShareOneGroupConfiguration() {
    InvokedMethodNameListener listener = run(FactoryGroupSample.class);

    // @BeforeClass and @AfterClass run once per instance, the group configuration once for the
    // whole group -- so the two cannot nest: @AfterGroups falls between the second instance's test
    // and its @AfterClass.
    assertThat(listener.getInvokedMethodNames())
        .containsExactly(
            "beforeClass",
            "beforeGroups",
            "test",
            "afterClass",
            "beforeClass",
            "test",
            "afterGroups",
            "afterClass");
  }

  @Test(description = "GITHUB-1346")
  public void groupConfigurationStaysInsideTheClassLifecycleWhenClassesRunInParallel() {
    XmlSuite suite = createXmlSuite("1346-parallel-classes");
    XmlTest test =
        createXmlTest(
            suite, "classes", MultiClassGroupAlphaSample.class, MultiClassGroupBravoSample.class);
    test.setParallel(XmlSuite.ParallelMode.CLASSES);
    suite.setThreadCount(2);

    List<String> invoked = run(suite).getInvokedMethodNames();

    assertThat(invoked).containsOnlyOnce("beforeGroups").containsOnlyOnce("afterGroups");
    assertThat(invoked)
        .containsSubsequence("alphaBeforeClass", "alphaTest", "alphaAfterClass")
        .containsSubsequence("bravoBeforeClass", "bravoTest", "bravoAfterClass");
    // Whichever class wins the race, the group is entered and left from inside a worker that has
    // already run its own @BeforeClass, so the first method invoked is never the group setup.
    assertThat(invoked.get(0)).isIn("alphaBeforeClass", "bravoBeforeClass");
    // Only the entering and leaving class are ordered against the group. Do not tighten this into
    // containsSubsequence("afterGroups", "bravoAfterClass"): over 600 runs of this suite the class
    // that finished first ran its @AfterClass before @AfterGroups in 66 of them.
  }

  @Test(description = "GITHUB-1346")
  public void groupConfigurationStaysInsideTheClassLifecycleWhenMethodsRunInParallel() {
    XmlSuite suite = createXmlSuite("1346-parallel-methods");
    XmlTest test = createXmlTest(suite, "methods", InterleavedGroupsSample.class);
    test.setParallel(XmlSuite.ParallelMode.METHODS);
    suite.setThreadCount(4);

    List<String> invoked = run(suite).getInvokedMethodNames();

    assertThat(invoked)
        .containsOnlyOnce("beforeClass")
        .containsOnlyOnce("afterClass")
        .containsOnlyOnce("beforeGroupsOne")
        .containsOnlyOnce("beforeGroupsTwo")
        .containsOnlyOnce("afterGroupsOne")
        .containsOnlyOnce("afterGroupsTwo");
    assertThat(invoked.get(0)).isEqualTo("beforeClass");
    assertThat(invoked.get(invoked.size() - 1)).isEqualTo("afterClass");
    assertThat(invoked)
        .containsSubsequence("beforeGroupsOne", "test1", "afterGroupsOne")
        .containsSubsequence("beforeGroupsTwo", "test2", "afterGroupsTwo");
  }
}
