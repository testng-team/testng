package test.groups.issue1834;

import org.testng.Reporter;
import org.testng.annotations.Test;

public class TestSample {
  @Test(groups = "Cached")
  public void dataSuccessfullyReceivedFromCache() {
    Reporter.log("Cached");
  }

  @Test(groups = "Uncached")
  public void dataSuccessfullyReceivedFromWhapi() {
    Reporter.log("Uncached");
  }
}
