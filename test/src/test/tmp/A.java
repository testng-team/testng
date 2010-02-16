package test.tmp;

import org.testng.ITest;
import org.testng.TestNG;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.reporters.EmailableReporter;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.Arrays;

public class A implements ITest {
  
  public static void main(String[] args) {
    XmlSuite s = new XmlSuite();
    s.setName("Synthetic suite");
    XmlTest t = new XmlTest(s);
    XmlClass c = new XmlClass(A.class);
    t.setXmlClasses(Arrays.asList(new XmlClass[] { c }));
    TestNG runner = new TestNG();
    runner.setXmlSuites(Arrays.asList(new XmlSuite[] { s }));
    runner.setUseDefaultListeners(false);

    // also tried this:
//            listeners.add(new
//    org.testng.reporters.TestHTMLReporter.class);
//            listeners.add(org.testng.reporters.SuiteHTMLReporter.class);
//            listeners.add(org.testng.reporters.EmailableReporter.class);
//    runner.addListener(new TestHTMLReporter());
//    runner.addListener(new SuiteHTMLReporter());
    runner.addListener(new EmailableReporter() );
    runner.run();
  }
  private int m_n;

  @DataProvider
  public Object[][] dp() {
    return new Object[][] { 
      new Object[] { 0 },  
      new Object[] { 1 },
      new Object[] { 2 },  
    };
  }

  public A(int n) {
    m_n = n;
  }

  private void log(String s) {
    System.out.println(" Instance " + m_n + " " + s);
  }

  @Override
  public String toString() {
    return "[A n:" + m_n + "]";
  }

//  @Override
//  public boolean equals(Object other) {
//    if (other == null) return false;
//    if (other == this) return true;
//    return ((A) other).m_n == m_n;
//  }

  @BeforeClass
  public void bc() {
    log("beforeClass");
  }

  @AfterClass
  public void ac() {
    log("afterClass");
  }

  @Test
  public void f1() {
    log("f1");
  }

  @Test
  public void f2() {
    log("f2");
  }
//  @Test(priority = 1)
//  public void f1() {
//  }
//
//  @Test(priority = -1)
//  public void f2() {
//  }
//
//  @Test
//  public void f3() {
//  }

  //  @BeforeSuite
//  public void bs() {
//    System.out.println("Before suite");
//  }
//
//  @AfterSuite
//  public void as() {
//    System.out.println("After suite");
//  }
//
//  @BeforeClass
//  public void beforeClassA() {
//  }
//
//  @AfterClass
//  public void afterClassA() {
//  }
//
//  @Test
//  public void g1() {
////    throw new RuntimeException();
//  }


  public String getTestName() {
    return "This is test A";
  }

//  @Test(groups = "mytest", dependsOnMethods = "g")
//  public void f() {
//  }
//
//
//  @AfterMethod
//  public void after() {
//  }

}
