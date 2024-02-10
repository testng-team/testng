package test.guice.issue3050;

import com.google.inject.Inject;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.annotations.Guice;

@Guice(modules = EvidenceModule.class)
public class EvidenceRetryAnalyzer implements IRetryAnalyzer {

  public static Set<UUID> uuids = ConcurrentHashMap.newKeySet();

  public static boolean hasOnlyOneUuid() {
    return uuids.size() == 1;
  }

  @Inject private UUID injectedUUID;

  @Override
  public boolean retry(ITestResult result) {
    uuids.add(injectedUUID);
    return false;
  }
}
