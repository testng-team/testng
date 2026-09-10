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
    assertThat(m_methods)
        .containsExactly(
            // Each class runs its own chain in order.
            b + "sameNameAA",
            b + "uniqueNameBB",
            b + "uniqueNameCC",
            b + "uniqueNameDD",
            b + "sameNameE",
            a + "sameNameA",
            a + "uniqueNameB",
            a + "uniqueNameC",
            a + "uniqueNameD",
            a + "sameNameE",
            // sameNameF waits for ClassA.sameNameE, not for ClassB.sameNameE, which ran earlier.
            a + "sameNameF",
            a + "sameNameG",
            a + "sameNameH");
  }
}
