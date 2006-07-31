package test.expectedexceptions;

import test.BaseTest;

import java.lang.reflect.Method;

import java.util.logging.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.testng.ITestListener;
import org.testng.ITestResult;

public class ExpectedExceptionsTest extends BaseTest {
  
  /**
   * @testng.test
   */
  public void expectedExceptions() {
    addClass("test.expectedexceptions.SampleExceptions");
    run();
    String[] passed = {
      "shouldPass",
    };
    String[] failed = {
        "shouldFail1", "shouldFail2"
    };
    String[] skipped = {
    };
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
    verifyTests("Skipped", skipped, getSkippedTests());
  }

}


