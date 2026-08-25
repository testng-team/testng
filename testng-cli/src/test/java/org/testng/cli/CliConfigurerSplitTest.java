package org.testng.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Field;
import java.util.List;
import org.testng.TestNG;
import org.testng.annotations.Test;

/**
 * What {@code -testclass} and {@code -testnames} do with the commas the user types. Nothing else
 * covers it: {@link CliConfigurerParityTest} compares the two configurers against each other, so a
 * change applied to both is invisible to it.
 *
 * <p>The class names are JDK types on purpose. What is under test is the splitting, and the loader
 * only has to answer something for each piece.
 */
public class CliConfigurerSplitTest {

  @Test
  public void aSingleClassNameIsTheOnlyClass() {
    assertThat(testClassesOf("java.lang.String")).containsExactly(String.class);
  }

  @Test
  public void severalClassNamesAreSplitOnTheComma() {
    assertThat(testClassesOf("java.lang.String,java.lang.Integer"))
        .containsExactly(String.class, Integer.class);
  }

  @Test
  public void aTrailingCommaContributesNoClass() {
    assertThat(testClassesOf("java.lang.String,")).containsExactly(String.class);
  }

  /** Today the space is part of the name, so the class is looked up as " java.lang.Integer". */
  @Test
  public void aSpaceAfterTheCommaIsPartOfTheName() {
    assertThatThrownBy(() -> testClassesOf("java.lang.String, java.lang.Integer"))
        .hasMessageContaining("Cannot load class from file:  java.lang.Integer");
  }

  /** Today an empty value is a class name, and the loader is asked for a class called "". */
  @Test
  public void anEmptyValueIsAskedOfTheClassLoader() {
    assertThatThrownBy(() -> testClassesOf(""))
        .hasMessageContaining("Cannot load class from file: ");
  }

  @Test
  public void aNameThatResolvesToNothingIsStillRejected() {
    assertThatThrownBy(() -> testClassesOf("com.acme.NoSuchClass"))
        .hasMessageContaining("Cannot load class from file: com.acme.NoSuchClass");
  }

  /** -testnames splits the same way, so the space is part of the name there too. */
  @Test
  public void testNamesAreSplitTheSameWay() {
    CliOptions cli = new CliOptions();
    cli.testNames = "t1, t2,";
    TestNG testng = new TestNG();
    CliConfigurer.configure(testng, cli);

    assertThat(this.<List<String>>read(testng, "m_testNames")).containsExactly("t1", " t2");
  }

  private static Class<?>[] testClassesOf(String testClass) {
    CliOptions cli = new CliOptions();
    cli.testClass = testClass;
    TestNG testng = new TestNG();
    CliConfigurer.configure(testng, cli);

    Class<?>[] classes = new CliConfigurerSplitTest().read(testng, "m_commandLineTestClasses");
    return classes == null ? new Class<?>[0] : classes;
  }

  @SuppressWarnings("unchecked")
  private <T> T read(TestNG testng, String fieldName) {
    try {
      Field field = TestNG.class.getDeclaredField(fieldName);
      field.setAccessible(true);
      return (T) field.get(testng);
    } catch (ReflectiveOperationException e) {
      throw new AssertionError("cannot read TestNG." + fieldName, e);
    }
  }
}
