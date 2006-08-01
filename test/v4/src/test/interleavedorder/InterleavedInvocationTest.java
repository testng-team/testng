package test.interleavedorder;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

import test.BaseTest;

import testhelper.OutputDirectoryPatch;


public class InterleavedInvocationTest extends BaseTest {
  public static List<String> LOG = new ArrayList<String>();

  @Configuration(beforeTest = true)
  public void beforeTest() {
    LOG = new ArrayList<String>();
  }

  @Test
  public void invocationOrder() {
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG testng = new TestNG();
    testng.setOutputDirectory(OutputDirectoryPatch.getOutputDirectory());
    testng.setTestClasses(new Class[] { TestChild1.class, TestChild2.class });
    testng.addListener(tla);
    testng.setVerbose(0);
    testng.run();
    
    final String log= LOG.toString();
    final String clsName= TestChild1.class.getName();
        
    List<String> expected = new ArrayList<String>();
    expected.add("beforeTestChild1Class");
    expected.add("test1");
    expected.add("test2");    
    expected.add("afterTestChild1Class");
    
    expected.add("beforeTestChild2Class");
    expected.add("test1");
    expected.add("test2");    
    expected.add("afterTestChild2Class");
    Assert.assertEquals(LOG, expected);
  }

  private void ppp(String s) {
    System.out.println("[InterleavedTest] " + s);
  }
}
