package test.retryAnalyzer.issue2798;

import java.util.ArrayList;
import java.util.List;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class HashCodeAwareRetryAnalyzer implements IRetryAnalyzer {

  public static final List<Integer> hashCodes = new ArrayList<>();

  int cnt = 0;
  static final int threshold = 2;

  @Override
  public synchronized boolean retry(ITestResult result) {
    hashCodes.add(this.hashCode());
    return cnt++ < threshold;
  }
}
