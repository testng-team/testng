package test.dataprovider.issue2819;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.testng.IDataProviderMethod;
import org.testng.IRetryDataProvider;

public class SimpleRetry implements IRetryDataProvider {

  private static final Set<String> objectIds = new HashSet<>();

  public static Set<String> getObjectIds() {
    return Collections.unmodifiableSet(objectIds);
  }

  public static void clearObjectIds() {
    objectIds.clear();
  }

  public SimpleRetry() {
    objectIds.add(UUID.randomUUID().toString());
  }

  private int counter = 0;

  @Override
  public boolean retry(IDataProviderMethod dataProvider) {
    return counter++ < 2;
  }
}
