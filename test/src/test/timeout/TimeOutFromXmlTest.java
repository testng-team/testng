package test.timeout;

import org.testng.annotations.Test;

import test.BaseTest;

public class TimeOutFromXmlTest extends BaseTest {
    
    private void timeOutTest(boolean onSuite) {
        addClass("test.timeout.TestTimeOutSampleTest");
        if (onSuite) {
            setSuiteTimeOut(1000);
        } else {
            setTestTimeOut(1000);
        }
        run();
        String[] passed = {
          };
        String[] failed = {
          "sleepsForFiveSeconds"
        };
      
//        dumpResults("Passed", getPassedTests());
//        dumpResults("Failed", getFailedTests());

        verifyTests("Passed", passed, getPassedTests());
        verifyTests("Failed", failed, getFailedTests());
    }

    @Test
    public void timeOutOnSuiteTag() {
        timeOutTest(true /* on suite */);
    }

    @Test
    public void timeOutOnTestTag() {
        timeOutTest(false /* on test */);
    }

    @Test
    public void noTimeOut() {
      addClass("test.timeout.TestTimeOutSampleTest");
      run();
      String[] passed = {
          "sleepsForFiveSeconds"
        };
        String[] failed = {
        };
        
        verifyTests("Passed", passed, getPassedTests());
        verifyTests("Failed", failed, getFailedTests());
    }
}
