package test.dataprovider.issue2111;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.testng.IDataProviderInterceptor;
import org.testng.IDataProviderMethod;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;

public class LocalDataProviderInterceptor implements IDataProviderInterceptor {

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
    list.remove(1);
    return list.iterator();
  }
}
