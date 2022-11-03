package test.dataprovider.issue2819;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.testng.IDataProviderMethod;
import org.testng.IRetryDataProvider;

public class SimpleRetry implements IRetryDataProvider {

  private static final Set<Integer> hashCodes = new HashSet<>();

  public static Set<Integer> getHashCodes() {
    return Collections.unmodifiableSet(hashCodes);
  }

  public SimpleRetry() {
    hashCodes.add(hashCode());
  }

  private int counter = 0;

  @Override
  public boolean retry(IDataProviderMethod dataProvider) {
    return counter++ < 2;
  }
}
