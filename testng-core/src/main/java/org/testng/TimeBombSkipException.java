package org.testng;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.jspecify.annotations.Nullable;

/**
 * Skips a test until a date, and fails it after that date.
 *
 * <p>Throw this exception from a test to skip it for a time. Up to the expiration date, TestNG
 * reports the test as skipped. After that date, TestNG reports it as failed. The message then adds
 * the date by which the test should have been enabled. The printed stack trace holds only the place
 * where the exception was created.
 *
 * <p>The default date format is {@code yyyy/MM/dd}. Some constructors take a format of your own.
 * Each format follows the rules of {@code SimpleDateFormat}.
 *
 * <p>The format that reads the date also sets how exactly TestNG compares the dates. TestNG cuts
 * the current time down to that format before it compares. For example, with {@code yyyy/MM/dd},
 * the test stays skipped for the whole expiration day.
 *
 * @since 5.6
 */
public class TimeBombSkipException extends SkipException {

  private static final long serialVersionUID = -8599821478834048537L;

  private static final String FORMAT = "yyyy/MM/dd";
  private final SimpleDateFormat sdf = new SimpleDateFormat(FORMAT);
  private final Calendar m_expireDate;
  private DateFormat m_inFormat = sdf;
  private DateFormat m_outFormat = sdf;

  /**
   * Creates the exception with an expiration date. The date format is {@code yyyy/MM/dd}.
   *
   * @param msg the reason for the skip.
   * @param expirationDate the date after which the skip becomes a failure.
   */
  public TimeBombSkipException(String msg, Date expirationDate) {
    this(msg, expirationDate, FORMAT);
  }

  /**
   * Creates the exception with an expiration date and a date format.
   *
   * @param msg the reason for the skip.
   * @param expirationDate the date after which the skip becomes a failure.
   * @param format the format that TestNG uses to compare the dates and to show the date.
   */
  public TimeBombSkipException(String msg, Date expirationDate, String format) {
    super(msg);
    m_inFormat = new SimpleDateFormat(format);
    m_outFormat = new SimpleDateFormat(format);
    m_expireDate = expireDateOf(expirationDate);
  }

  /**
   * Creates the exception with an expiration date in the format {@code yyyy/MM/dd}.
   *
   * @param msg the reason for the skip.
   * @param date the date after which the skip becomes a failure.
   * @throws TestNGException when {@code date} does not match the format.
   */
  public TimeBombSkipException(String msg, String date) {
    super(msg);
    m_expireDate = expireDateOf(date);
  }

  /**
   * Creates the exception with an expiration date in the given format.
   *
   * @param msg the reason for the skip.
   * @param date the date after which the skip becomes a failure.
   * @param format the format of {@code date}. TestNG also uses it to compare the dates and to show
   *     the date.
   * @throws TestNGException when {@code date} does not match {@code format}.
   */
  public TimeBombSkipException(String msg, String date, String format) {
    this(msg, date, format, format);
  }

  /**
   * Creates the exception with an expiration date, and separate formats to read and to show the
   * date.
   *
   * @param msg the reason for the skip.
   * @param date the date after which the skip becomes a failure.
   * @param inFormat the format of {@code date}. TestNG also uses it to compare the dates.
   * @param outFormat the format that the message uses to show the date.
   * @throws TestNGException when {@code date} does not match {@code inFormat}.
   */
  public TimeBombSkipException(String msg, String date, String inFormat, String outFormat) {
    super(msg);
    m_inFormat = new SimpleDateFormat(inFormat);
    m_outFormat = new SimpleDateFormat(outFormat);
    m_expireDate = expireDateOf(date);
  }

  /**
   * Creates the exception with an expiration date and a cause. The date format is {@code
   * yyyy/MM/dd}.
   *
   * @param msg the reason for the skip.
   * @param expirationDate the date after which the skip becomes a failure.
   * @param cause the exception that caused the skip.
   */
  public TimeBombSkipException(String msg, Date expirationDate, Throwable cause) {
    super(msg, cause);
    m_expireDate = expireDateOf(expirationDate);
  }

  /**
   * Creates the exception with an expiration date, a date format and a cause.
   *
   * @param msg the reason for the skip.
   * @param expirationDate the date after which the skip becomes a failure.
   * @param format the format that TestNG uses to compare the dates and to show the date.
   * @param cause the exception that caused the skip.
   */
  public TimeBombSkipException(String msg, Date expirationDate, String format, Throwable cause) {
    super(msg, cause);
    m_inFormat = new SimpleDateFormat(format);
    m_outFormat = new SimpleDateFormat(format);
    m_expireDate = expireDateOf(expirationDate);
  }

  /**
   * Creates the exception with an expiration date in the format {@code yyyy/MM/dd}, and a cause.
   *
   * @param msg the reason for the skip.
   * @param date the date after which the skip becomes a failure.
   * @param cause the exception that caused the skip.
   * @throws TestNGException when {@code date} does not match the format.
   */
  public TimeBombSkipException(String msg, String date, Throwable cause) {
    super(msg, cause);
    m_expireDate = expireDateOf(date);
  }

  /**
   * Creates the exception with an expiration date in the given format, and a cause.
   *
   * @param msg the reason for the skip.
   * @param date the date after which the skip becomes a failure.
   * @param format the format of {@code date}. TestNG also uses it to compare the dates and to show
   *     the date.
   * @param cause the exception that caused the skip.
   * @throws TestNGException when {@code date} does not match {@code format}.
   */
  public TimeBombSkipException(String msg, String date, String format, Throwable cause) {
    this(msg, date, format, format, cause);
  }

  /**
   * Creates the exception with an expiration date, separate formats to read and to show the date,
   * and a cause.
   *
   * @param msg the reason for the skip.
   * @param date the date after which the skip becomes a failure.
   * @param inFormat the format of {@code date}. TestNG also uses it to compare the dates.
   * @param outFormat the format that the message uses to show the date.
   * @param cause the exception that caused the skip.
   * @throws TestNGException when {@code date} does not match {@code inFormat}.
   */
  public TimeBombSkipException(
      String msg, String date, String inFormat, String outFormat, Throwable cause) {
    super(msg, cause);
    m_inFormat = new SimpleDateFormat(inFormat);
    m_outFormat = new SimpleDateFormat(outFormat);
    m_expireDate = expireDateOf(date);
  }

  private static Calendar expireDateOf(Date expireDate) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(expireDate);
    return calendar;
  }

  private Calendar expireDateOf(String date) {
    try {
      return expireDateOf(m_inFormat.parse(date));
    } catch (ParseException pex) {
      throw new TestNGException("Cannot parse date:" + date + " using pattern: " + m_inFormat, pex);
    }
  }

  @Override
  public boolean isSkip() {
    try {
      Calendar now = Calendar.getInstance();
      Date nowDate = m_inFormat.parse(m_inFormat.format(now.getTime()));
      now.setTime(nowDate);

      return !now.after(m_expireDate);
    } catch (ParseException pex) {
      throw new TestNGException("Cannot compare dates.");
    }
  }

  @Override
  public @Nullable String getMessage() {
    if (isSkip()) {
      return super.getMessage();
    } else {
      return super.getMessage()
          + "; Test must have been enabled by: "
          + m_outFormat.format(m_expireDate.getTime());
    }
  }

  @Override
  public void printStackTrace(PrintStream s) {
    reduceStackTrace();
    super.printStackTrace(s);
  }

  @Override
  public void printStackTrace(PrintWriter s) {
    reduceStackTrace();
    super.printStackTrace(s);
  }
}
