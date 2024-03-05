package test.retryAnalyzer.issue2798;

import java.util.ArrayList;
import java.util.List;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.internal.AutoCloseableLock;

public class HashCodeAwareRetryAnalyzer implements IRetryAnalyzer {

  public static final List<Integer> hashCodes = new ArrayList<>();

  private static final AutoCloseableLock lock = new AutoCloseableLock();

  int cnt = 0;
  static final int threshold = 2;

  @Override
  public boolean retry(ITestResult result) {
    try (AutoCloseableLock ignore = lock.lock()) {
      hashCodes.add(this.hashCode());
      return cnt++ < threshold;
    }
  }
}
