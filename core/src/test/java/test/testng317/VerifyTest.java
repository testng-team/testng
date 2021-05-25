package test.testng317;

import org.testng.TestNG;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

import java.util.ArrayList;
import java.util.List;

public class VerifyTest extends SimpleBaseTest {
  static List<String> m_methods = new ArrayList<>();

  @BeforeMethod
  public void before() {
    m_methods = new ArrayList<>();
  }

  @Test
  public void verify() {
    TestNG tng = create();
    tng.setTestClasses(new Class[] { test.testng317.ClassB.class, test.testng317.ClassA.class });
    tng.run();

    System.out.println("Methods:" + m_methods.size());
  }
}
