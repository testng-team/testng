package test.annotationtransformer;

import java.util.List;

import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

public class AnnotationTransformerTest {
  
  /**
   * Make sure that without a transformer in place, a class-level
   * annotation invocationCount is correctly used.
   */
  @Test
  public void verifyAnnotationWithoutTransformer() {
      TestNG tng = new TestNG();
      tng.setVerbose(0);
      tng.setTestClasses(new Class[] { AnnotationTransformerClassInvocationSampleTest.class});
      TestListenerAdapter tla = new TestListenerAdapter();
      tng.addListener(tla);
      
      tng.run();
      
      List passed = tla.getPassedTests();
      Assert.assertEquals(passed.size(), 6);
  }

  /**
   * Test a transformer on a method-level @Test
   */
  @Test
  public void verifyAnnotationTransformerMethod() {
      TestNG tng = new TestNG();
      tng.setVerbose(0);
      tng.setAnnotationTransformer(new MyTransformer());
      tng.setTestClasses(new Class[] { AnnotationTransformerSampleTest.class});
      TestListenerAdapter tla = new TestListenerAdapter();
      tng.addListener(tla);
      
      tng.run();
      
      List passed = tla.getPassedTests();
      Assert.assertEquals(passed.size(), 15);
  }
  
  /**
   * Test a transformer on a class-level @Test
   */
  @Test
  public void verifyAnnotationTransformerClass() {
      TestNG tng = new TestNG();
      tng.setVerbose(0);
      tng.setAnnotationTransformer(new MyTimeOutTransformer());
      tng.setTestClasses(new Class[] { AnnotationTransformerClassSampleTest.class});
      TestListenerAdapter tla = new TestListenerAdapter();
      tng.addListener(tla);
      
      tng.run();
      
      List<ITestResult> passed = tla.getPassedTests();
      Assert.assertEquals(passed.size(), 1);
      Assert.assertEquals("one", passed.get(0).getMethod().getMethodName());
  }


}
