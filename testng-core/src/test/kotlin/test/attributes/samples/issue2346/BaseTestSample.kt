package test.attributes.samples.issue2346

import java.util.UUID
import org.testng.ITestResult
import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Listeners

@Listeners(LocalTestListener::class)
open class BaseTestSample {

  @BeforeMethod(alwaysRun = true)
  fun setUp(result: ITestResult ) {
    val session = UUID.randomUUID().toString()
      result.setAttribute("session", session)
  }

  @AfterMethod(alwaysRun = true)
  fun tearDown(iTestResult: ITestResult ) {
    val key = "tearDown_" + iTestResult.method.qualifiedName
        data.putIfAbsent(key, iTestResult.attributeNames.isEmpty())
    }
}
