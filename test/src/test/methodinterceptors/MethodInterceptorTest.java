package test.methodinterceptors;

import org.testng.Assert;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MethodInterceptorTest {
  
  @Test
  public void noMethodsShouldRun() {
    TestNG tng = new TestNG();
    tng.setTestClasses(new Class[] { FooTest.class });
    tng.setVerbose(0);
    tng.setMethodInterceptor(new NullMethodInterceptor());
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    tng.run();
    
    Assert.assertEquals(tla.getPassedTests().size(), 0);
    Assert.assertEquals(tla.getFailedTests().size(), 0);
    Assert.assertEquals(tla.getSkippedTests().size(), 0);
  }
  
  @Test
  public void fastShouldRunFirst() {
    TestNG tng = new TestNG();
    tng.setTestClasses(new Class[] { FooTest.class });
    tng.setVerbose(0);
    tng.setMethodInterceptor(new IMethodInterceptor() {
      public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
        List<IMethodInstance> result = new ArrayList<IMethodInstance>();
        for (IMethodInstance m : methods) {
          Test test = m.getMethod().getMethod().getAnnotation(Test.class);
          Set<String> groups = new HashSet<String>();
          for (String group : test.groups()) {
            groups.add(group);
          }
          if (groups.contains("fast")) {
            result.add(0, m);
          }
          else {
            result.add(m);
          }
        }
        return result;
      }
    });
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    tng.run();
    
    Assert.assertEquals(tla.getPassedTests().size(), 2);
    ITestResult first = tla.getPassedTests().get(0);
    Assert.assertEquals(first.getMethod().getMethodName(), "fast");
  }

}
