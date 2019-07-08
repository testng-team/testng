package org.testng;

import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.internal.annotations.DisabledRetryAnalyzer;

/**
 * A {@link SkipException} extension that transforms a skipped test method into a failed method, if field {@code checkRerun} is 
 * set as true, meanwhile test method has customized retryAnalyzer. 
 */
public class SkipWithRetryCheckException extends SkipException {
    
    private static final long serialVersionUID = 1233817787139806976L;
    private volatile boolean checkRerun = true;
    
    public SkipWithRetryCheckException(String skipMessage) {
      this(skipMessage, true);
    }

    public SkipWithRetryCheckException(String skipMessage, Throwable cause) {
      this(skipMessage, cause, true);
    }
    
    public SkipWithRetryCheckException(String skipMessage, boolean checkRerun) {
      super(skipMessage);
      this.checkRerun = checkRerun;
    }
    
    public SkipWithRetryCheckException(String skipMessage, Throwable cause, boolean checkRerun) {
      super(skipMessage, cause);
      this.checkRerun = checkRerun;
    }

    @Override
    public boolean isSkip() {
      ITestResult currentTestResult = Reporter.getCurrentTestResult();
      if((this.checkRerun)&&(currentTestResult != null)&&(!currentTestResult.getMethod().getRetryAnalyzer(currentTestResult).getClass()
              .getSimpleName().equals(DisabledRetryAnalyzer.class.getSimpleName()))) {
        return false;
      }
      return true;
    }

}
