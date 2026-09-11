package org.testng.dependent;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.testng.TestNG;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.dependent.samples.sharednames.ClassA;
import org.testng.dependent.samples.sharednames.ClassB;
import org.testng.dependent.samples.sharednames.DeclaredDependencyRecorder;
import test.SimpleBaseTest;

/**
 * Checks that {@code dependsOnMethods} picks the method in its own class when another class in the
 * same run declares one of that name.
 *
 * <p>{@link ClassA} and {@link ClassB} both declare {@code sameNameE}. {@code ClassA.sameNameF}
 * names {@code "sameNameE"} and nothing else, so only the declaring class tells the two apart.
 *
 * <p>The name is not ambiguous to TestNG, because {@code DependencyMap} keys on the qualified name
 * and the two are separate keys. The choice still matters: renaming {@code ClassA.sameNameE} makes
 * {@code sameNameF} resolve to {@code ClassB.sameNameE} instead, with no error. So a change that
 * made the lookup less specific would be silent, and this test is what would catch it.
 */
public class DependsOnMethodsWithSharedNamesTest extends SimpleBaseTest {
  public static List<String> m_methods = new ArrayList<>();

  @BeforeMethod
  public void before() {
    m_methods = new ArrayList<>();
  }

  @Test
  public void sameNameFDependsOnItsOwnClassOnly() {
    TestNG tng = create();
    tng.setTestClasses(new Class[] {ClassB.class, ClassA.class});
    DeclaredDependencyRecorder recorder = new DeclaredDependencyRecorder();
    tng.setMethodInterceptor(recorder);
    tng.run();

    String a = ClassA.class.getName() + ".";
    String b = ClassB.class.getName() + ".";

    // The dependency itself, read before scheduling. An order assertion cannot say this: sameNameF
    // runs after ClassB.sameNameE whether or not it waits for it, because ClassB runs first.
    assertThat(recorder.declaredFor(a + "sameNameF")).containsExactly(a + "sameNameE");
    assertThat(recorder.declaredFor(a + "sameNameF")).doesNotContain(b + "sameNameE");
  }

  @Test
  public void eachChainRunsInItsOwnOrder() {
    TestNG tng = create();
    tng.setTestClasses(new Class[] {ClassB.class, ClassA.class});
    tng.run();

    String a = ClassA.class.getName() + ".";
    String b = ClassB.class.getName() + ".";

    // Nothing ties the two chains to each other, so the order between them is not asserted.
    assertThat(m_methods)
        .containsSubsequence(
            b + "sameNameAA",
            b + "uniqueNameBB",
            b + "uniqueNameCC",
            b + "uniqueNameDD",
            b + "sameNameE");
    assertThat(m_methods)
        .containsSubsequence(
            a + "sameNameA",
            a + "uniqueNameB",
            a + "uniqueNameC",
            a + "uniqueNameD",
            a + "sameNameE",
            a + "sameNameF",
            a + "sameNameG",
            a + "sameNameH");
    assertThat(m_methods).hasSize(13).doesNotHaveDuplicates();
  }
}
