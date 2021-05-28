package test.retryAnalyzer;

import org.testng.ITestResult;
import org.testng.util.RetryAnalyzerCount;

public class MyRetry extends  RetryAnalyzerCount {

	public MyRetry(){
		setCount(3);
	}

	@Override
	public boolean retryMethod(ITestResult arg0) {
		// TODO Auto-generated method stub
		return true;
	}

}
