package test.reports.issue1259;

import java.util.Iterator;
import java.util.stream.IntStream;
import org.testng.Reporter;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * A run whose report is large because it holds many results, which is the shape GITHUB-2334
 * describes -- a data provider feeding hundreds of thousands of invocations of one method.
 *
 * <p>The row count is read from a system property so the same sample can be calibrated without
 * being recompiled. Each invocation logs a couple of lines, so the reporter panel carries real
 * report content rather than padding: {@code Reporter.getOutput} is what {@code ReporterPanel}
 * renders.
 */
public class LargeReportSample {

  public static final String ROWS_PROPERTY = "testng.test.issue1259.rows";

  /**
   * An iterator rather than an array: the array form holds every row at once before the first test
   * runs, which at the result counts this models is a slice of the very heap under measurement --
   * and it is the provider, not the reporter, that would run out of it first.
   */
  @DataProvider(name = "rows")
  public Iterator<Object[]> rows() {
    int rows = Integer.getInteger(ROWS_PROPERTY, 1000);
    return IntStream.range(0, rows).mapToObj(i -> new Object[] {"row-" + i}).iterator();
  }

  @Test(dataProvider = "rows")
  public void report(String row) {
    Reporter.log("started " + row);
    Reporter.log("finished " + row);
  }
}
