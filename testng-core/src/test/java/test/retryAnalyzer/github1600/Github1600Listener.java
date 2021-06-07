package test.retryAnalyzer.github1600;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.testng.IAnnotationTransformer;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.annotations.ITestAnnotation;
import org.testng.internal.annotations.DisabledRetryAnalyzer;

public class Github1600Listener implements IInvokedMethodListener, IAnnotationTransformer {
  @Override
  public void beforeInvocation(IInvokedMethod iInvokedMethod, ITestResult iTestResult) {}

  @Override
  public void afterInvocation(IInvokedMethod iInvokedMethod, ITestResult iTestResult) {
    if (iInvokedMethod.isTestMethod()) {
      String attribute = Github1600Analyzer.NO;
      if (iTestResult.getStatus() == ITestResult.SUCCESS) {
        iTestResult.setStatus(ITestResult.FAILURE);
        attribute = Github1600Analyzer.YES;
      }
      iTestResult.setAttribute(Github1600Analyzer.RETRY, attribute);
    }
  }

  @Override
  public void transform(
      ITestAnnotation iTestAnnotation, Class aClass, Constructor constructor, Method method) {
    Class<? extends IRetryAnalyzer> retry = iTestAnnotation.getRetryAnalyzerClass();
    if (retry.equals(DisabledRetryAnalyzer.class)) {
      iTestAnnotation.setRetryAnalyzer(Github1600Analyzer.class);
    }
  }
}
