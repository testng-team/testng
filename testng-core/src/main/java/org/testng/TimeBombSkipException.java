package org.testng;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * A {@link SkipException} extension that transforms a skipped method into a failed method based on
 * a time trigger.
 *
 * <p>By default the time format is yyyy/MM/dd (according to {@code SimpleDateFormat}). You can
 * customize this by using the specialized constructors. Suppported date formats are according to
 * the {@code SimpleDateFormat}.
 *
 * @since 5.6
 */
public class TimeBombSkipException extends SkipException {

  private static final long serialVersionUID = -8599821478834048537L;

  private static final String FORMAT = "yyyy/MM/dd";
  private final SimpleDateFormat sdf = new SimpleDateFormat(FORMAT);
  private Calendar m_expireDate;
  private DateFormat m_inFormat = sdf;
  private DateFormat m_outFormat = sdf;

  /**
   * Creates a {@code TimeBombedSkipException} using the <code>expirationDate</code>. The format
   * used for date comparison is <code>yyyy/MM/dd</code>
   *
   * @param msg exception message
   * @param expirationDate time limit after which the SKIP becomes a FAILURE
   */
  public TimeBombSkipException(String msg, Date expirationDate) {
    this(msg, expirationDate, FORMAT);
  }

  /**
   * Creates a {@code TimeBombedSkipException} using the <code>expirationDate</code>. The <code>
   * format</code> parameter wiil be used for performing the time comparison.
   *
   * @param msg exception message
   * @param expirationDate time limit after which the SKIP becomes a FAILURE
   * @param format format for the time comparison
   */
  public TimeBombSkipException(String msg, Date expirationDate, String format) {
    super(msg);
    m_inFormat = new SimpleDateFormat(format);
    m_outFormat = new SimpleDateFormat(format);
    initExpireDate(expirationDate);
  }

  /**
   * Creates a {@code TimeBombedSkipException} using the <code>date</code> in the format <code>
   * yyyy/MM/dd</code>.
   *
   * @param msg exception message
   * @param date time limit after which the SKIP becomes a FAILURE
   */
  public TimeBombSkipException(String msg, String date) {
    super(msg);
    initExpireDate(date);
  }

  /**
   * Creates a {@code TimeBombedSkipException} using the <code>date</code> in the specified format
   * <code>format</code>. The same format is used when performing the time comparison.
   *
   * @param msg exception message
   * @param date time limit after which the SKIP becomes a FAILURE
   * @param format format of the passed in <code>date</code> and of the time comparison
   */
  public TimeBombSkipException(String msg, String date, String format) {
    this(msg, date, format, format);
  }

  /**
   * Creates a {@code TimeBombedSkipException} using the <code>date</code> in the specified format
   * <code>inFormat</code>. The <code>outFormat</code> will be used to perform the time comparison
   * and display.
   *
   * @param msg exception message
   * @param date time limit after which the SKIP becomes a FAILURE
   * @param inFormat format of the passed in <code>date</code>
   * @param outFormat format of the time comparison
   */
  public TimeBombSkipException(String msg, String date, String inFormat, String outFormat) {
    super(msg);
    m_inFormat = new SimpleDateFormat(inFormat);
    m_outFormat = new SimpleDateFormat(outFormat);
    initExpireDate(date);
  }

  /**
   * Creates a {@code TimeBombedSkipException} using the <code>expirationDate</code>. The format
   * used for date comparison is <code>yyyy/MM/dd</code>
   *
   * @param msg exception message
   * @param expirationDate time limit after which the SKIP becomes a FAILURE
   * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).
   *     (A <code>null</code> value is permitted, and indicates that the cause is nonexistent or
   *     unknown.)
   */
  public TimeBombSkipException(String msg, Date expirationDate, Throwable cause) {
    super(msg, cause);
    initExpireDate(expirationDate);
  }

  /**
   * Creates a {@code TimeBombedSkipException} using the <code>expirationDate</code>. The <code>
   * format</code> parameter wiil be used for performing the time comparison.
   *
   * @param msg exception message
   * @param expirationDate time limit after which the SKIP becomes a FAILURE
   * @param format format for the time comparison
   * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).
   *     (A <code>null</code> value is permitted, and indicates that the cause is nonexistent or
   *     unknown.)
   */
  public TimeBombSkipException(String msg, Date expirationDate, String format, Throwable cause) {
    super(msg, cause);
    m_inFormat = new SimpleDateFormat(format);
    m_outFormat = new SimpleDateFormat(format);
    initExpireDate(expirationDate);
  }

  /**
   * Creates a {@code TimeBombedSkipException} using the <code>date</code> in the format <code>
   * yyyy/MM/dd</code>.
   *
   * @param msg exception message
   * @param date time limit after which the SKIP becomes a FAILURE
   * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).
   *     (A <code>null</code> value is permitted, and indicates that the cause is nonexistent or
   *     unknown.)
   */
  public TimeBombSkipException(String msg, String date, Throwable cause) {
    super(msg, cause);
    initExpireDate(date);
  }

  /**
   * Creates a {@code TimeBombedSkipException} using the <code>date</code> in the specified format
   * <code>format</code>. The same format is used when performing the time comparison.
   *
   * @param msg exception message
   * @param date time limit after which the SKIP becomes a FAILURE
   * @param format format of the passed in <code>date</code> and of the time comparison
   * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).
   *     (A <code>null</code> value is permitted, and indicates that the cause is nonexistent or
   *     unknown.)
   */
  public TimeBombSkipException(String msg, String date, String format, Throwable cause) {
    this(msg, date, format, format, cause);
  }

  /**
   * Creates a {@code TimeBombedSkipException} using the <code>date</code> in the specified format
   * <code>inFormat</code>. The <code>outFormat</code> will be used to perform the time comparison
   * and display.
   *
   * @param msg exception message
   * @param date time limit after which the SKIP becomes a FAILURE
   * @param inFormat format of the passed in <code>date</code>
   * @param outFormat format of the time comparison
   * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).
   *     (A <code>null</code> value is permitted, and indicates that the cause is nonexistent or
   *     unknown.)
   */
  public TimeBombSkipException(
      String msg, String date, String inFormat, String outFormat, Throwable cause) {
    super(msg, cause);
    m_inFormat = new SimpleDateFormat(inFormat);
    m_outFormat = new SimpleDateFormat(outFormat);
    initExpireDate(date);
  }

  private void initExpireDate(Date expireDate) {
    m_expireDate = Calendar.getInstance();
    m_expireDate.setTime(expireDate);
  }

  private void initExpireDate(String date) {
    try {
      Date d = m_inFormat.parse(date);
      initExpireDate(d);
    } catch (ParseException pex) {
      throw new TestNGException("Cannot parse date:" + date + " using pattern: " + m_inFormat, pex);
    }
  }

  @Override
  public boolean isSkip() {
    if (null == m_expireDate) {
      return false;
    }

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
  public String getMessage() {
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
