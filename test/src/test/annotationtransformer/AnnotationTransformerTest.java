package test.annotationtransformer;

import java.util.List;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

public class AnnotationTransformerTest {

  @Test
  public void verifyAnnotationTransformer() {
      TestNG tng = new TestNG();
      tng.setVerbose(0);
      tng.setAnnotationTransformer(new MyTransformer());
      tng.setTestClasses(new Class[] { AnnotationTransformerSampleTest.class});
      TestListenerAdapter tla = new TestListenerAdapter();
      tng.addListener(tla);
      
      tng.run();
      
      List passed = tla.getPassedTests();
      Assert.assertEquals(15, passed.size());
  }
}
