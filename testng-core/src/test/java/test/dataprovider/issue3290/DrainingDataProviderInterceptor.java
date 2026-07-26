package test.dataprovider.issue3290;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.testng.IDataProviderInterceptor;
import org.testng.IDataProviderMethod;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;

/**
 * An interceptor that fully drains the original data provider iterator into a list, drops the first
 * row and returns a brand-new iterator. This mirrors the common interceptor pattern and is used to
 * confirm that a resource-backed {@code Stream} is still closed even though the iterator the test
 * actually runs against is not the one derived from the stream.
 */
public class DrainingDataProviderInterceptor implements IDataProviderInterceptor {

  @Override
  public Iterator<Object[]> intercept(
      Iterator<Object[]> original,
      IDataProviderMethod dataProviderMethod,
      ITestNGMethod method,
      ITestContext iTestContext) {
    Iterable<Object[]> iterable = () -> original;
    List<Object[]> list =
        StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());
    list.remove(0);
    return list.iterator();
  }
}
