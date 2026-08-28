package org.testng.internal.reporters;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import org.testng.annotations.Test;
import org.testng.internal.Utils;
import org.testng.reporters.snapshot.NoRenderingParameterSample.Speechless;
import org.testng.reporters.snapshot.ParameterShapesSample.Shape;
import org.testng.reporters.snapshot.UnrenderableParameterSample.Unrenderable;

/**
 * The shapes a captured value is written in, and what keeps them honest.
 *
 * <p>The other shapes are derived from the capture rather than from the object, so that the user's
 * {@code toString()} runs once for every report. {@link Utils#toString(Object, Class)} and {@link
 * Utils#toString(Object)} are what every reporter called to build them before there was anywhere to
 * keep a rendering, and the first two tests here are what hold the derivations to answering the
 * same thing. They live in another module, so nothing but these tests stops the copy drifting.
 */
public class ParameterValueTest {

  /** Every shape the representations disagree about, plus one they all agree on. */
  private static final List<Object[]> SHAPES =
      Arrays.asList(
          new Object[] {"text", String.class},
          new Object[] {"", String.class},
          new Object[] {"42", String.class},
          new Object[] {42, int.class},
          new Object[] {null, Object.class},
          new Object[] {new int[] {1, 2}, int[].class},
          new Object[] {new String[] {"a", "b"}, String[].class},
          new Object[] {new Shape(), Shape.class},
          new Object[] {new Speechless(), Speechless.class});

  @Test(description = "The console rendering is the one Utils has always produced")
  public void theConsoleRenderingIsTheOneUtilsProduces() {
    for (Object[] shape : SHAPES) {
      Object value = shape[0];
      Class<?> type = (Class<?>) shape[1];
      assertThat(ParameterValue.of(value, type).rendered())
          .as("console rendering of %s declared as %s", value, type)
          .isEqualTo(Utils.toString(value, type));
    }
  }

  @Test(description = "The plain rendering is the one Utils has always produced")
  public void thePlainRenderingIsTheOneUtilsProduces() {
    for (Object[] shape : SHAPES) {
      Object value = shape[0];
      Class<?> type = (Class<?>) shape[1];
      // Including the value nothing was supplied for: Utils answers the word for it, and so does
      // this -- which is the whole difference between the plain form and the XML one.
      assertThat(ParameterValue.of(value, type).plain())
          .as("plain rendering of %s declared as %s", value, type)
          .isEqualTo(Utils.toString(value));
    }
  }

  @Test(description = "The XML shape carries no console decoration")
  public void theXmlValueIsTheValueItself() {
    assertThat(ParameterValue.of("text", String.class).value()).isEqualTo("text");
    assertThat(ParameterValue.of("", String.class).value()).isEmpty();
    assertThat(ParameterValue.of(42, int.class).value()).isEqualTo("42");
    assertThat(ParameterValue.of(new int[] {1, 2}, int[].class).value()).isEqualTo("[1, 2]");
    assertThat(ParameterValue.of(new String[] {"a", "b"}, String[].class).value())
        .isEqualTo("[a, b]");
    assertThat(ParameterValue.of(new Shape(), Shape.class).value()).isEqualTo("shape");
  }

  @Test(description = "A value that was not supplied is told apart from the string \"null\"")
  public void anAbsentValueIsNotTheWord() {
    ParameterValue absent = ParameterValue.of(null, Object.class);
    assertThat(absent.isNull()).isTrue();
    assertThat(absent.value()).isNull();
    // Neither the console nor the HTML reports have an attribute to say it with, so both say the
    // word -- which is where the plain form parts company with the XML one.
    assertThat(absent.rendered()).isEqualTo("null");
    assertThat(absent.plain()).isEqualTo("null");

    ParameterValue word = ParameterValue.of("null", String.class);
    assertThat(word.isNull()).isFalse();
    assertThat(word.value()).isEqualTo("null");
    assertThat(word.rendered()).isEqualTo("\"null\"");
    // The HTML reports cannot tell the two apart, and never could: both read "null" there.
    assertThat(word.plain()).isEqualTo("null");
  }

  @Test(
      description =
          "An object whose toString() answers null was supplied, so it is not reported as an"
              + " absent value")
  public void aValueWithNoRenderingIsStillAValue() {
    ParameterValue speechless = ParameterValue.of(new Speechless(), Speechless.class);

    assertThat(speechless.isNull()).isFalse();
    // Left as it came: XMLStringBuffer.addCDATA writes the word for it, which is what the XML
    // report has always contained for such an object.
    assertThat(speechless.value()).isNull();
    // The plain form has the same hole, deliberately: it is Utils.toString(Object) exactly, and
    // that is what its callers were reading before. Each of them says what it writes for it.
    assertThat(speechless.plain()).isNull();
  }

  @Test(
      description =
          "GITHUB-2830: a value whose toString() throws was supplied, so it is not reported as an"
              + " absent one -- every shape carries the identity Utils fell back to")
  public void aValueThatCannotRenderItselfIsStillAValue() {
    Unrenderable unrenderable = new Unrenderable();

    ParameterValue value = ParameterValue.of(unrenderable, Unrenderable.class);

    // Where the fallback itself comes from is UtilsTest's to pin; what matters here is that it
    // reaches both representations and that neither reads it as a value the invocation never got.
    String identity = identityOf(unrenderable);
    assertThat(value.isNull()).isFalse();
    assertThat(value.value()).isEqualTo(identity);
    assertThat(value.rendered()).isEqualTo(identity);
    assertThat(value.plain()).isEqualTo(identity);
  }

  /** What {@link Object#toString()} would have answered for a class that does not override it. */
  private static String identityOf(Object value) {
    return value.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(value));
  }
}
