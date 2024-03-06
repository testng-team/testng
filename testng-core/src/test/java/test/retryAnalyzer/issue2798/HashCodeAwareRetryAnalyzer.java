package test.retryAnalyzer.issue2798;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.internal.AutoCloseableLock;

public class HashCodeAwareRetryAnalyzer implements IRetryAnalyzer {

  public static final List<String> hashCodes = new ArrayList<>();

  private static final AutoCloseableLock lock = new AutoCloseableLock();

  private final UUID id = UUID.randomUUID();

  int cnt = 0;
  static final int threshold = 2;

  @Override
  public boolean retry(ITestResult result) {
    try (AutoCloseableLock ignore = lock.lock()) {
      hashCodes.add(id.toString());
      return cnt++ < threshold;
    }
  }
}
