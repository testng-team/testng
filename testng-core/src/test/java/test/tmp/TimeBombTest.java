package test.tmp;

import java.lang.reflect.Method;
import java.util.Calendar;
import org.testng.annotations.Test;
import org.testng.log4testng.Logger;

public class TimeBombTest {

  @IgnoreUntil(time = "1022")
  @Test
  public void timeBomb() throws SecurityException, NoSuchMethodException {
    Method m = TimeBombTest.class.getMethod("timeBomb");
    IgnoreUntil t = m.getAnnotation(IgnoreUntil.class);
    long now = Calendar.getInstance().getTimeInMillis();
    long l = parseTime(t.time());
    debug("IGNORE:" + (now < l));
  }

  private long parseTime(String string) {
    int hour = Integer.parseInt(string.substring(0, 2));
    int minute = Integer.parseInt(string.substring(2));
    Calendar result = Calendar.getInstance();
    result.set(Calendar.HOUR_OF_DAY, hour);
    result.set(Calendar.MINUTE, minute);

    return result.getTimeInMillis();
  }

  private void debug(String string) {
    Logger.getLogger(getClass()).debug("[TimeBombTest] " + string);
  }
}
