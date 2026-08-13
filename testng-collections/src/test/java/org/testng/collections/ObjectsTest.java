package org.testng.collections;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

/**
 * Characterization tests for {@link Objects.ToStringHelper}.
 *
 * <p>{@code omitNulls()} and {@code omitEmptyStrings()} are dead code, and {@code
 * org.testng.internal.TestResult} is their only caller -- so what they do (nothing) shows up in
 * reports through {@code TestResult.toString()}. That is recorded here rather than fixed.
 */
public class ObjectsTest {

  private static class Sample {}

  @Test
  public void rendersTheSimpleClassNameFollowedByTheValues() {
    String actual = Objects.toStringHelper(Sample.class).add("a", "1").add("b", "2").toString();

    assertThat(actual).isEqualTo("[Sample a=1 b=2]");
  }

  @Test
  public void rendersAnEmptyClassNameForAnAnonymousClass() {
    Object anonymous = new Object() {};

    String actual = Objects.toStringHelper(anonymous.getClass()).add("a", "1").toString();

    assertThat(actual).isEqualTo("[ a=1]");
  }

  @Test
  public void rendersNullAsABraceLiteral() {
    assertThat(Objects.toStringHelper(Sample.class).add("a", (Object) null).toString())
        .isEqualTo("[Sample a={null}]");
  }

  @Test
  public void bothAddOverloadsRenderNullIdentically() {
    String viaString = Objects.toStringHelper(Sample.class).add("a", (String) null).toString();
    String viaObject = Objects.toStringHelper(Sample.class).add("a", (Object) null).toString();

    assertThat(viaString).isEqualTo(viaObject);
  }

  @Test
  public void rendersAnEmptyStringAsAPairOfQuotes() {
    assertThat(Objects.toStringHelper(Sample.class).add("a", "").toString())
        .isEqualTo("[Sample a=\"\"]");
  }

  @Test
  public void omitNullsDoesNotOmitAnything() {
    // add() stores s(value), and s(null) returns the *string* "{null}", so ValueHolder.isNull() is
    // never true and the flag has no effect.
    String actual =
        Objects.toStringHelper(Sample.class)
            .omitNulls()
            .add("a", (Object) null)
            .add("b", "2")
            .toString();

    assertThat(actual).isEqualTo("[Sample a={null} b=2]");
  }

  @Test
  public void omitEmptyStringsDoesNotOmitAnything() {
    // s("") returns "\"\"", which Strings.isNullOrEmpty does not consider empty.
    String actual =
        Objects.toStringHelper(Sample.class)
            .omitEmptyStrings()
            .add("a", "")
            .add("b", "2")
            .toString();

    assertThat(actual).isEqualTo("[Sample a=\"\" b=2]");
  }

  @Test
  public void rendersAnEmptyHelperAsBareBrackets() {
    assertThat(Objects.toStringHelper(Sample.class).toString()).isEqualTo("[Sample ]");
  }
}
