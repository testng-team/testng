package org.testng.collections;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

/** Tests for {@link Objects.ToStringHelper}. */
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
  public void omitNullsDropsNullValues() {
    String actual =
        Objects.toStringHelper(Sample.class)
            .omitNulls()
            .add("a", (Object) null)
            .add("b", "2")
            .toString();

    assertThat(actual).isEqualTo("[Sample b=2]");
  }

  @Test
  public void omitEmptyStringsDropsEmptyStrings() {
    String actual =
        Objects.toStringHelper(Sample.class)
            .omitEmptyStrings()
            .add("a", "")
            .add("b", "2")
            .toString();

    assertThat(actual).isEqualTo("[Sample b=2]");
  }

  @Test
  public void omitFiltersTogetherDropNullAndEmptyAndKeepTheRest() {
    String actual =
        Objects.toStringHelper(Sample.class)
            .omitNulls()
            .omitEmptyStrings()
            .add("a", "v")
            .add("b", (Object) null)
            .add("c", "")
            .toString();

    assertThat(actual).isEqualTo("[Sample a=v]");
  }

  @Test
  public void omitFiltersDoNotGlueTheNextValueToTheClassName() {
    String actual =
        Objects.toStringHelper(Sample.class)
            .omitNulls()
            .add("skipped", (Object) null)
            .add("kept", "v")
            .toString();

    assertThat(actual).isEqualTo("[Sample kept=v]");
  }

  @Test
  public void omitFiltersKeepASingleSpaceBetweenSurvivingValues() {
    String actual =
        Objects.toStringHelper(Sample.class)
            .omitNulls()
            .omitEmptyStrings()
            .add("a", "1")
            .add("skipped", (Object) null)
            .add("b", "2")
            .toString();

    assertThat(actual).isEqualTo("[Sample a=1 b=2]");
  }

  @Test
  public void rendersAnEmptyHelperAsBareBrackets() {
    assertThat(Objects.toStringHelper(Sample.class).toString()).isEqualTo("[Sample ]");
  }

  @Test
  public void omittingEveryValueLooksLikeAnEmptyHelper() {
    String actual =
        Objects.toStringHelper(Sample.class)
            .omitNulls()
            .omitEmptyStrings()
            .add("a", (Object) null)
            .add("b", "")
            .toString();

    assertThat(actual).isEqualTo("[Sample ]");
  }
}
