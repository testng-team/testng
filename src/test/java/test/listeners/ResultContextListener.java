package test.listeners;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class ResultContextListener implements ITestListener {
	
	public static boolean contextProvided = false;
	
	public void onTestStart(ITestResult result) {
		ITestContext context = result.getTestContext();
		if (context != null) {
			contextProvided = true;
		}
	}

	public void onTestSuccess(ITestResult result) {
	}

	public void onTestFailure(ITestResult result) {
	}

	public void onTestSkipped(ITestResult result) {
	}

	public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
	}

	public void onStart(ITestContext context) {
	}

	public void onFinish(ITestContext context) {
	}

}
