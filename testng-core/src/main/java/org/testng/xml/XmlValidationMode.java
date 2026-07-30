package org.testng.xml;

import java.util.Arrays;
import org.testng.internal.RuntimeBehavior;
import org.testng.log4testng.Logger;

/**
 * How strictly a suite file is checked against the TestNG DTD, selected with the {@code
 * testng.xml.validation} system property.
 *
 * <p>Validation used to be silently disabled: {@code XMLParser} probed the SAX validation feature
 * under an {@code https} identifier, which no parser recognizes, so {@code setValidating(true)} was
 * never reached and DTD violations went unreported. Turning it back on means suite files that have
 * been accepted for years can suddenly be rejected -- the DTD constrains the order of the children
 * of {@code <suite>}, for instance -- so {@link #WARN} is the default for now and reports
 * violations without failing the run.
 */
public enum XmlValidationMode {

  /** Do not validate at all. */
  OFF,

  /** Validate and report violations as warnings. The default. */
  WARN,

  /** Validate and fail on the first violation. */
  STRICT;

  private static final XmlValidationMode DEFAULT = WARN;

  public boolean isValidating() {
    return this != OFF;
  }

  /**
   * The mode requested by the {@code testng.xml.validation} system property, falling back to {@link
   * #WARN} when the property is absent or holds an unknown value.
   */
  public static XmlValidationMode current() {
    String requested = RuntimeBehavior.getXmlValidationMode();
    if (requested == null || requested.trim().isEmpty()) {
      return DEFAULT;
    }
    String normalized = requested.trim().toUpperCase();
    return Arrays.stream(values())
        .filter(mode -> mode.name().equals(normalized))
        .findFirst()
        .orElseGet(
            () -> {
              Logger.getLogger(XmlValidationMode.class)
                  .warn(
                      "Unknown value ["
                          + requested
                          + "] for the system property ["
                          + RuntimeBehavior.XML_VALIDATION_MODE
                          + "]. Expected one of "
                          + Arrays.toString(values())
                          + ". Falling back to ["
                          + DEFAULT
                          + "].");
              return DEFAULT;
            });
  }
}
