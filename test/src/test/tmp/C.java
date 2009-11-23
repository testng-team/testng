package test.tmp;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

public class C extends SimpleBaseTest {
  @Test
  public void f() {
    TestNG tng = create(A.class);
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    tng.run();
    
    System.out.println("Passed:" + tla.getPassedTests());
  }
}
