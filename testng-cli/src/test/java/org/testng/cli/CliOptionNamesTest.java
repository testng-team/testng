package org.testng.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.TreeMap;
import org.testng.CommandLineArgs;
import org.testng.annotations.Test;

/**
 * {@link CliOptions} owns the canonical option names, but the deprecated {@link CommandLineArgs}
 * still declares its own copy for the Maven Surefire integration. They must not drift apart while
 * both exist.
 */
public class CliOptionNamesTest {

  @Test
  public void optionNamesMatchTheDeprecatedCommandLineArgs() {
    assertThat(constantsOf(CliOptions.class))
        .as("option constants of %s", CliOptions.class.getName())
        .isEqualTo(constantsOf(CommandLineArgs.class));
  }

  private static Map<String, Object> constantsOf(Class<?> type) {
    Map<String, Object> constants = new TreeMap<>();
    for (Field field : type.getDeclaredFields()) {
      int modifiers = field.getModifiers();
      if (!Modifier.isStatic(modifiers) || !Modifier.isPublic(modifiers)) {
        continue;
      }
      try {
        constants.put(field.getName(), field.get(null));
      } catch (IllegalAccessException e) {
        throw new AssertionError("Cannot read " + type.getName() + "." + field.getName(), e);
      }
    }
    return constants;
  }
}
