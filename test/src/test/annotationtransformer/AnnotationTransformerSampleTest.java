package test.annotationtransformer;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;

public class AnnotationTransformerSampleTest {

  private int m_five = 0;
  private int m_three = 0;
  private int m_four = 0;

  @Test(invocationCount = 5)
  public void four() {
    m_four++;
    System.out.println("Should be invoked 4 times");
  }
  
  @Test(invocationCount = 5)
  public void three() {
    m_three++;
    System.out.println("Should be invoked 3 times");
  }

  @Test(invocationCount = 5)
  public void five() {
    m_five++;
    System.out.println("Should be invoked 5 times");
  }
  
  @Test(dependsOnMethods = {"three", "four", "five"})
  public void verify() {
    Assert.assertEquals(m_three, 3);
    Assert.assertEquals(m_four, 4);
    Assert.assertEquals(m_five, 5);
    
  }

  public static void main(String[] argv) {
    TestNG tng = new TestNG();
    tng.setAnnotationTransformer(new MyTransformer());
    tng.setTestClasses(new Class[] { AnnotationTransformerSampleTest.class});
    
    tng.run();
  }
}
