package test.tmp;

import org.testng.IAnnotationTransformer;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class AnnotationTransformer 
  implements IAnnotationTransformer, ITestListener
{

  public void transform(ITestAnnotation annotation, Class testClass,
      Constructor testConstructor, Method testMethod) 
  {
  }

  public void onFinish(ITestContext context) {
    // TODO Auto-generated method stub
    
  }

  public void onStart(ITestContext context) {
    // TODO Auto-generated method stub
    
  }

  public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
    // TODO Auto-generated method stub
    
  }

  public void onTestFailure(ITestResult result) {
    // TODO Auto-generated method stub
    
  }

  public void onTestSkipped(ITestResult result) {
    // TODO Auto-generated method stub
    
  }

  public void onTestStart(ITestResult result) {
    // TODO Auto-generated method stub
    
  }

  public void onTestSuccess(ITestResult result) {
    // TODO Auto-generated method stub
    
  }

}
