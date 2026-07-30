package org.testng.xml;

import java.util.Arrays;
import java.util.Locale;
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
 *
 * <p>The property is read at two different moments, which constrains when it can be changed. {@code
 * XMLParser} decides <em>whether to validate</em> once, when it builds its single static {@code
 * SAXParser}; {@code TestNGContentHandler.error} decides <em>how to report</em> a violation on
 * every occurrence. Moving between {@link #WARN} and {@link #STRICT} at run time therefore takes
 * effect, but moving away from {@link #OFF} does not, because no violation is ever raised to
 * report. Set the property on the command line to be safe.
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
    try {
      return valueOf(requested.trim().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException e) {
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
    }
  }
}
