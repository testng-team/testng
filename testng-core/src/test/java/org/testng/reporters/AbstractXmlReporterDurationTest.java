package org.testng.reporters;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.Properties;
import org.testng.annotations.Test;
import org.testng.util.TimeUtils;

/**
 * What {@code started-at}, {@code finished-at} and {@code duration-ms} are computed from in
 * testng-results.xml. Nothing covered it, and the three attributes are the only reason the reporter
 * reads a test context's timestamps at all.
 */
public class AbstractXmlReporterDurationTest {

  private static final long START_MILLIS = 1_700_000_000_000L;
  private static final long END_MILLIS = START_MILLIS + 1_234L;

  @Test
  public void theDurationIsTheEndMinusTheStartInMilliseconds() {
    assertThat(durationAttributes().getProperty(XMLReporterConfig.ATTR_DURATION_MS))
        .isEqualTo("1234");
  }

  @Test
  public void theTimestampsAreTheStartAndTheEndInTheConfiguredFormat() {
    XMLReporterConfig config = new XMLReporterConfig();
    Properties attributes = durationAttributes();

    assertThat(attributes.getProperty(XMLReporterConfig.ATTR_STARTED_AT))
        .isEqualTo(
            TimeUtils.formatTimeInLocalOrSpecifiedTimeZone(
                START_MILLIS, config.getTimestampFormat()));
    assertThat(attributes.getProperty(XMLReporterConfig.ATTR_FINISHED_AT))
        .isEqualTo(
            TimeUtils.formatTimeInLocalOrSpecifiedTimeZone(
                END_MILLIS, config.getTimestampFormat()));
  }

  /** A test that started and never ended is reported as lasting no time, not as a negative one. */
  @Test
  public void aRunThatEndedWhereItStartedLastsNoTime() {
    Properties attributes = new Properties();
    AbstractXmlReporter.setDurationAttributes(
        new XMLReporterConfig(), attributes, new Date(START_MILLIS), new Date(START_MILLIS));

    assertThat(attributes.getProperty(XMLReporterConfig.ATTR_DURATION_MS)).isEqualTo("0");
  }

  private static Properties durationAttributes() {
    Properties attributes = new Properties();
    AbstractXmlReporter.setDurationAttributes(
        new XMLReporterConfig(), attributes, new Date(START_MILLIS), new Date(END_MILLIS));
    return attributes;
  }
}
