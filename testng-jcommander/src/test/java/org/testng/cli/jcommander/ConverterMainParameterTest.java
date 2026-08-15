package org.testng.cli.jcommander;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import java.lang.reflect.Field;
import java.util.List;
import org.testng.annotations.Test;

/**
 * {@link Converter}'s main parameter starts out as an empty list rather than as {@code null}, so
 * that the class can be null-marked without annotating a field JCommander always fills. Both halves
 * of "the seeding is invisible" are pinned here, because both are JCommander's behaviour rather
 * than TestNG's: a seeded list is reused and cleared, and {@code required} is decided from the
 * parameter description rather than from the field being null.
 */
public class ConverterMainParameterTest {

  @Test
  public void aSeededMainParameterStillEnforcesRequired() {
    assertThatThrownBy(() -> new JCommander(new Converter()).parse())
        .isInstanceOf(ParameterException.class);
  }

  @Test
  public void aSeededMainParameterCollectsExactlyTheValuesGiven() throws Exception {
    Converter converter = new Converter();

    new JCommander(converter).parse("a.xml", "b.yaml");

    assertThat(mainParameterOf(converter)).containsExactly("a.xml", "b.yaml");
  }

  @SuppressWarnings("unchecked")
  private static List<String> mainParameterOf(Converter converter) throws Exception {
    Field field = Converter.class.getDeclaredField("m_files");
    field.setAccessible(true);
    return (List<String>) field.get(converter);
  }
}
