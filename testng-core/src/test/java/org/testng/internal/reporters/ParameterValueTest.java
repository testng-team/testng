package org.testng.internal.reporters;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.testng.annotations.Test;
import org.testng.internal.Utils;
import org.testng.reporters.snapshot.ParameterShapesSample.Shape;

/**
 * The two shapes a captured value is written in, and what keeps them honest.
 *
 * <p>The console shape is derived from the capture rather than from the object, so that the user's
 * {@code toString()} runs once for both reports. {@link Utils#toString(Object, Class)} is what
 * every reporter called to build it before there was anywhere to keep a rendering, and the first
 * test here is what holds the derivation to answering the same thing.
 */
public class ParameterValueTest {

  /** Every shape the two representations disagree about, plus one they agree on. */
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
    // The console has no attribute to say it with, so it says the word.
    assertThat(absent.rendered()).isEqualTo("null");

    ParameterValue word = ParameterValue.of("null", String.class);
    assertThat(word.isNull()).isFalse();
    assertThat(word.value()).isEqualTo("null");
    assertThat(word.rendered()).isEqualTo("\"null\"");
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
  }

  /** A parameter with nothing to say, which is not the same as not being there. */
  private static final class Speechless {

    @Override
    public @Nullable String toString() {
      return null;
    }
  }
}
