package test.attributes.issue2346;

import java.util.UUID;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

@Listeners(LocalTestListener.class)
public class BaseTest {

  @BeforeMethod(alwaysRun = true)
  public void setUp(ITestResult result) {
    String session = UUID.randomUUID().toString();
    result.setAttribute("session", session);
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown(ITestResult iTestResult) {
    String key = "tearDown_" + iTestResult.getMethod().getQualifiedName();
    LocalTestListener.data.putIfAbsent(key, iTestResult.getAttributeNames().isEmpty());
  }
}
