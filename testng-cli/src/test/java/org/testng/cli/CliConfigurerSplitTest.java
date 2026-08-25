package org.testng.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Field;
import java.util.List;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * What {@code -testclass} and {@code -testnames} do with the commas the user types. {@link
 * org.testng.internal.Utils#splitCommaSeparated} owns the contract and {@code UtilsTest} asserts
 * it; what this covers is that the command line is wired to it, which nothing else does — {@link
 * CliConfigurerParityTest} compares the two configurers against each other, so a change applied to
 * both is invisible to it.
 *
 * <p>The class names are JDK types on purpose. What is under test is the splitting, and the loader
 * only has to answer something for each piece.
 */
public class CliConfigurerSplitTest {

  @Test(dataProvider = "testClassValues")
  public void theTestClassOptionIsSplitOnCommas(String value, Class<?>[] expected) {
    assertThat(testClassesOf(value)).containsExactly(expected);
  }

  @DataProvider(name = "testClassValues")
  public Object[][] testClassValues() {
    return new Object[][] {
      {"java.lang.String", new Class<?>[] {String.class}},
      {"java.lang.String,java.lang.Integer", new Class<?>[] {String.class, Integer.class}},
      {"java.lang.String,", new Class<?>[] {String.class}},
      {"java.lang.String, java.lang.Integer", new Class<?>[] {String.class, Integer.class}},
      {"", new Class<?>[0]},
      {" , ,", new Class<?>[0]},
    };
  }

  @Test
  public void aNameThatResolvesToNothingIsStillRejected() {
    assertThatThrownBy(() -> testClassesOf("com.acme.NoSuchClass"))
        .hasMessageContaining("Cannot load class from file: com.acme.NoSuchClass");
  }

  /** -testnames goes through the same helper, so the pieces are trimmed there too. */
  @Test
  public void testNamesAreSplitTheSameWay() {
    CliOptions cli = new CliOptions();
    cli.testNames = "t1, t2,";
    TestNG testng = new TestNG();
    CliConfigurer.configure(testng, cli);

    List<String> names = read(testng, "m_testNames");
    assertThat(names).containsExactly("t1", "t2");
  }

  private static Class<?>[] testClassesOf(String testClass) {
    CliOptions cli = new CliOptions();
    cli.testClass = testClass;
    TestNG testng = new TestNG();
    CliConfigurer.configure(testng, cli);

    Class<?>[] classes = read(testng, "m_commandLineTestClasses");
    return classes == null ? new Class<?>[0] : classes;
  }

  @SuppressWarnings("unchecked")
  private static <T> T read(TestNG testng, String fieldName) {
    try {
      Field field = TestNG.class.getDeclaredField(fieldName);
      field.setAccessible(true);
      return (T) field.get(testng);
    } catch (ReflectiveOperationException e) {
      throw new AssertionError("cannot read TestNG." + fieldName, e);
    }
  }
}
