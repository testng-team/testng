package test.xml.duplicate;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;
import test.xml.duplicate.sample.DuplicateOccurrenceSample;
import test.xml.duplicate.sample.OuterWithNestedSample;

/**
 * The DTD lets {@code <class>} repeat inside {@code <classes>} and {@code <include>} repeat inside
 * {@code <methods>}, and both tags carry their own {@code <parameter>}. Each occurrence is a
 * distinct run of the method with its own parameters.
 */
public class DuplicateXmlOccurrenceTest extends SimpleBaseTest {

  @Test
  public void sameClassTwiceRunsOncePerOccurrence() {
    assertThat(runSuite("xml/duplicate/duplicate-classes-parameters.xml"))
        .containsExactlyInAnyOrder("f(one)", "f(two)");
  }

  @Test
  public void sameIncludeTwiceRunsOncePerOccurrence() {
    assertThat(runSuite("xml/duplicate/duplicate-includes-parameters.xml"))
        .containsExactlyInAnyOrder("f(one)", "f(two)");
  }

  @Test
  public void eachOccurrenceSelectsItsOwnMethods() {
    assertThat(runSuite("xml/duplicate/duplicate-classes-distinct-methods.xml"))
        .containsExactlyInAnyOrder("f(fromSuite)", "g(fromSuite)");
  }

  /**
   * A scanned {@code <package>} is not a second occurrence of a class the {@code <test>} also lists
   * outright: the {@code <class>} tag is the one that describes it, parameters, {@code <methods>}
   * and all.
   */
  @Test
  public void aScannedPackageDoesNotDuplicateAnExplicitClass() {
    assertThat(runSuite("xml/duplicate/package-and-class.xml")).containsExactly("f(fromClassTag)");
  }

  /**
   * The fixtures above go through the XML parser, which is the only thing that numbers the tags. A
   * suite assembled through the API has every index at zero, and is also the only way to reach the
   * {@code <include>} to {@code <class>} back-pointer, which no parser sets.
   */
  @Test
  public void occurrencesOfAProgrammaticSuiteAreDistinctToo() {
    XmlSuite suite = createXmlSuite("suite");
    suite.getParameters().put("id", "fromSuite");
    XmlTest test = createXmlTest(suite, "test");
    XmlClass first = createXmlClass(test, DuplicateOccurrenceSample.class);
    createXmlInclude(first, "f", Collections.singletonMap("id", "one"));
    createXmlInclude(first, "f", Collections.singletonMap("id", "two"));
    XmlClass second =
        createXmlClass(test, DuplicateOccurrenceSample.class, Collections.singletonMap("id", "3"));
    createXmlInclude(second, "f");
    createXmlInclude(second, "g");

    assertThat(run(true, create(suite)).getSucceedMethodNames())
        .containsExactlyInAnyOrder("f(one)", "f(two)", "f(3)", "g(3)");
  }

  /**
   * A nested class has no tag of its own here: it is the enclosing {@code <class>} tag that brings
   * it in, so it belongs to that tag and follows every occurrence of it.
   */
  @Test
  public void aNestedClassFollowsEveryOccurrenceOfItsEnclosingTag() {
    XmlSuite suite = createXmlSuite("suite");
    XmlTest test = createXmlTest(suite, "test");
    createXmlClass(test, OuterWithNestedSample.class, Collections.singletonMap("id", "one"));
    createXmlClass(test, OuterWithNestedSample.class, Collections.singletonMap("id", "two"));

    assertThat(run(true, create(suite)).getSucceedMethodNames())
        .containsExactlyInAnyOrder("outer(one)", "outer(two)", "nested(one)", "nested(two)");
  }

  /**
   * {@code XmlClass.clone()} hands its {@code <include>} list straight to the copy, so two
   * occurrences can share the very same {@code XmlInclude}. It therefore cannot be asked which
   * occurrence is running; the method carries both of its own tags.
   */
  @Test
  public void occurrencesSharingAnIncludeKeepTheirOwnClassParameters() {
    XmlSuite suite = createXmlSuite("suite");
    XmlTest test = createXmlTest(suite, "test");
    XmlClass first =
        createXmlClass(test, DuplicateOccurrenceSample.class, Collections.singletonMap("id", "A"));
    XmlClass second =
        createXmlClass(test, DuplicateOccurrenceSample.class, Collections.singletonMap("id", "B"));
    List<XmlInclude> shared = Collections.singletonList(new XmlInclude("f"));
    first.setIncludedMethods(shared);
    second.setIncludedMethods(shared);

    assertThat(run(true, create(suite)).getSucceedMethodNames())
        .containsExactlyInAnyOrder("f(A)", "f(B)");
  }

  private static List<String> runSuite(String resource) {
    TestNG tng = create();
    tng.setTestSuites(Collections.singletonList(getPathToResource(resource)));
    return run(true, tng).getSucceedMethodNames();
  }
}
