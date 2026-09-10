package org.testng.dependent;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.testng.TestNG;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.dependent.samples.sharednames.ClassA;
import org.testng.dependent.samples.sharednames.ClassB;
import test.SimpleBaseTest;

/**
 * Checks that {@code dependsOnMethods} finds the method in its own class when another class in the
 * same run has a method of that name.
 *
 * <p>{@link ClassA} and {@link ClassB} both declare {@code sameNameE}. {@code ClassA.sameNameF}
 * depends on {@code "sameNameE"} by name alone, so the two are told apart only by the class that
 * declares them.
 */
public class DependsOnMethodsWithSharedNamesTest extends SimpleBaseTest {
  public static List<String> m_methods = new ArrayList<>();

  @BeforeMethod
  public void before() {
    m_methods = new ArrayList<>();
  }

  @Test
  public void eachChainKeepsItsOwnSameNamedMethod() {
    TestNG tng = create();
    tng.setTestClasses(new Class[] {ClassB.class, ClassA.class});
    tng.run();

    String b = ClassB.class.getName() + ".";
    String a = ClassA.class.getName() + ".";

    // Each chain runs in its own order. The two chains do not depend on each other, so nothing
    // fixes the order between them, and this does not assert one.
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
            // sameNameF waits for ClassA.sameNameE. ClassB declares a method of that name too,
            // and it does not release sameNameF.
            a + "sameNameE",
            a + "sameNameF",
            a + "sameNameG",
            a + "sameNameH");

    // ClassB.sameNameE is the wrong one, and it running first proves it was not what released
    // ClassA.sameNameF.
    assertThat(m_methods.indexOf(b + "sameNameE")).isLessThan(m_methods.indexOf(a + "sameNameF"));
    assertThat(m_methods).hasSize(13).doesNotHaveDuplicates();
  }
}
