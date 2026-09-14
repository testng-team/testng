package org.testng.reporters;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import javax.xml.parsers.DocumentBuilderFactory;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.util.TimeUtils;
import org.w3c.dom.Document;
import test.SimpleBaseTest;
import test.TestHelper;

/**
 * GITHUB-899: the JUnit {@code timestamp} attribute uses the configured timezone, not a GMT-only
 * formatter.
 */
public class JUnitReportTimestampTest extends SimpleBaseTest {

  private static final String TIMEZONE_PROPERTY = "testng.timezone";
  private static final String HONOLULU = "Pacific/Honolulu";
  private static final String GMT = "GMT";
  private static final long FIXED_MILLIS = 1_700_000_000_000L;

  @Test
  public void formattedTimeUsesTheConfiguredTimezoneRatherThanGmt() {
    String previous = System.getProperty(TIMEZONE_PROPERTY);
    TimeZone previousDefault = TimeZone.getDefault();
    try {
      // Force GMT so a fallback to TimeZone.getDefault cannot pass as Honolulu.
      TimeZone.setDefault(TimeZone.getTimeZone(GMT));
      System.setProperty(TIMEZONE_PROPERTY, HONOLULU);
      String formatted =
          TimeUtils.formatTimeInLocalOrSpecifiedTimeZone(
              FIXED_MILLIS, XMLReporterConfig.FMT_DEFAULT);

      assertThat(formatted).isEqualTo(formatInZone(FIXED_MILLIS, HONOLULU));
      assertThat(formatted).isNotEqualTo(formatInZone(FIXED_MILLIS, GMT));
    } finally {
      TimeZone.setDefault(previousDefault);
      restoreTimezone(previous);
    }
  }

  @Test
  public void junitReportReporterWritesATimezoneAwareTimestamp() throws Exception {
    String previous = System.getProperty(TIMEZONE_PROPERTY);
    TimeZone previousDefault = TimeZone.getDefault();
    try {
      TimeZone.setDefault(TimeZone.getTimeZone(GMT));
      System.setProperty(TIMEZONE_PROPERTY, HONOLULU);
      long before = System.currentTimeMillis();
      Path outputDir = TestHelper.createRandomDirectory();
      TestNG tng = create(outputDir, PassingSample.class);
      tng.addListener(new JUnitReportReporter());
      tng.run();
      long after = System.currentTimeMillis();

      File report =
          outputDir
              .resolve("junitreports")
              .resolve("TEST-" + PassingSample.class.getName() + ".xml")
              .toFile();
      assertThat(report).exists();

      String timestamp = timestampAttribute(report);
      long parsedMillis = parseMillis(timestamp);
      assertThat(parsedMillis).isBetween(before - 1_000L, after + 1_000L);
      assertThat(timestamp).isEqualTo(formatInZone(parsedMillis, HONOLULU));
      assertThat(timestamp).isNotEqualTo(formatInZone(parsedMillis, GMT));
    } finally {
      TimeZone.setDefault(previousDefault);
      restoreTimezone(previous);
    }
  }

  public static class PassingSample {
    @Test
    public void ok() {}
  }

  private static String formatInZone(long millis, String zoneId) {
    SimpleDateFormat format = new SimpleDateFormat(XMLReporterConfig.FMT_DEFAULT);
    format.setTimeZone(TimeZone.getTimeZone(zoneId));
    return format.format(millis);
  }

  @SuppressWarnings("JavaUtilDate")
  private static long parseMillis(String timestamp) throws ParseException {
    return new SimpleDateFormat(XMLReporterConfig.FMT_DEFAULT).parse(timestamp).getTime();
  }

  private static String timestampAttribute(File report) throws Exception {
    Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(report);
    return document.getDocumentElement().getAttribute(XMLConstants.ATTR_TIMESTAMP);
  }

  private static void restoreTimezone(String previous) {
    if (previous == null) {
      System.clearProperty(TIMEZONE_PROPERTY);
    } else {
      System.setProperty(TIMEZONE_PROPERTY, previous);
    }
  }
}
