package test.tmp;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;

import test.SimpleBaseTest;

public class B extends SimpleBaseTest {
  public static void main(String[] arg) {
    TestNG tng = new TestNG();
    tng.setTestClasses(new Class[] { C.class });
    tng.setParallel("method");
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    tng.run();
    
    System.out.println("\n# tests:" + tla.getPassedTests().size());
  }
}

