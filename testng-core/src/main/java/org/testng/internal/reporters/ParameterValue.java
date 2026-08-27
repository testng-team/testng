package org.testng.internal.reporters;

import org.jspecify.annotations.Nullable;
import org.testng.internal.Utils;

/**
 * One value an invocation ran with, captured while it was starting, in the shapes TestNG's built-in
 * reports write it in.
 *
 * <p>They are not the same string, and never have been. {@code TextReporter} and {@code
 * VerboseReporter} describe a value to a human reading a console: a {@code String} is quoted, so
 * that {@code "42"} is not read as a number and an empty one is visible at all. The XML report
 * serializes it for a machine, so it writes the value itself and says {@code is-null="true"} on the
 * element rather than writing the word. The HTML reports write the plain rendering, undecorated,
 * and spell an absent value {@code null} because they have no attribute to say it with.
 *
 * <p>What all three start from is the same single rendering, which is the point of capturing here
 * rather than in each reporter: {@link Utils#toString(Object)} calls the user's {@code toString()}
 * once, and the other forms are derived from its result rather than from the object. However many
 * built-in reports a run has enabled, a parameter is rendered once per invocation.
 *
 * <p>{@code ParameterValueTest} pins the console form against {@link Utils#toString(Object, Class)}
 * and the plain form against {@link Utils#toString(Object)}, which is what every reporter called
 * before there was anywhere to keep a rendering.
 *
 * <p>Nothing here guards the user's {@code toString()}: {@link Utils#toString(Object)} does, for
 * every caller rather than for this one, so a value that cannot render itself arrives here already
 * described by its identity. Both the moment this class renders at -- as the invocation starts --
 * and the moment a report falls back to rendering the values itself are covered by that, and both
 * answer the same string. A {@code toString()} that answers {@code null} rather than throwing is
 * not that case and is not guarded there; see {@link #plain()}.
 */
public final class ParameterValue {

  /** The one an invocation was given nothing for, which carries no state of its own. */
  private static final ParameterValue ABSENT = new ParameterValue(true, null, "null");

  private final boolean isNull;
  private final @Nullable String value;
  private final String rendered;

  private ParameterValue(boolean isNull, @Nullable String value, String rendered) {
    this.isNull = isNull;
    this.value = value;
    this.rendered = rendered;
  }

  /**
   * @param parameter - One of the values an invocation ran with.
   * @param parameterType - The type the method declares for it, which decides whether the console
   *     form quotes it.
   * @return - Both renderings of that value.
   */
  public static ParameterValue of(@Nullable Object parameter, Class<?> parameterType) {
    if (parameter == null) {
      return ABSENT;
    }
    @Nullable String value = Utils.toString(parameter);
    // Utils renders the object and then decorates the rendering. Handing it the rendering back
    // rather than the object runs the second half only: a String renders as itself, so the user's
    // toString() is not called again. An object with no rendering at all reads to Utils as an empty
    // one, and passing "" is what keeps that.
    return new ParameterValue(
        false, value, Utils.toString(value == null ? "" : value, parameterType));
  }

  /**
   * Whether the invocation was given no value here, which the XML report states as an attribute.
   */
  public boolean isNull() {
    return isNull;
  }

  /**
   * The value as the XML report writes it, with no console decoration: a {@code String} unquoted,
   * an array by its contents. Meaningless when {@link #isNull()}, and itself {@code null} for the
   * one value that has no rendering -- an object whose {@code toString()} answered {@code null},
   * which the XML report has always written as the word.
   */
  public @Nullable String value() {
    return value;
  }

  /** The value as {@code TextReporter} and {@code VerboseReporter} print it. */
  public String rendered() {
    return rendered;
  }

  /**
   * The single rendering itself, before either report decorates it: exactly what {@link
   * Utils#toString(Object)} answers for the value, including the word {@code null} for one the
   * invocation was given nothing for.
   *
   * <p>This is what the built-in HTML reports write -- {@code EmailableReporter2} and the {@code
   * jq} panels -- and it is the form every reporter called {@code Utils} for before there was
   * anywhere to keep a rendering. It is neither of the other two: {@link #rendered()} quotes a
   * {@code String} for a human reading a console, and {@link #value()} answers {@code null} for an
   * absent value because the XML report says so with an attribute instead.
   *
   * <p>Itself {@code null} for the one value that has no rendering -- an object whose {@code
   * toString()} answered {@code null} -- which is the same hole {@link Utils#toString(Object)} has,
   * and is what pinning the two together preserves. A caller that writes into a {@code
   * StringBuilder} or a {@code StringJoiner} gets the word for it either way; one that hands it to
   * a method rejecting {@code null} has to say what it writes.
   */
  public @Nullable String plain() {
    return isNull ? "null" : value;
  }
}
