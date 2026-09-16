package org.testng.parameters.samples.resolver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.testng.IDataProviderInterceptor;
import org.testng.IDataProviderMethod;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;

/** Hands the rows through the first time, and blanks every one of them on any later read. */
public class RowDroppingInterceptor implements IDataProviderInterceptor {

  private int reads = 0;

  @Override
  public Iterator<Object @Nullable []> intercept(
      Iterator<Object @Nullable []> original,
      IDataProviderMethod dataProviderMethod,
      ITestNGMethod method,
      @Nullable ITestContext iTestContext) {
    boolean blank = ++reads > 1;
    List<Object @Nullable []> rows = new ArrayList<>();
    while (original.hasNext()) {
      Object @Nullable [] row = original.next();
      rows.add(blank ? null : row);
    }
    return rows.iterator();
  }
}
