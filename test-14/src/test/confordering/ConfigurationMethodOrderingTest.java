package test.confordering;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.TestNG;

import test.confordering.subpckg1.Parent;
import test.confordering.subpckg2.NonOverriddingChild;
import test.confordering.subpckg2.OverriddingChild;
import test.confordering.subpckg2.SimpleOverriddingChild;

import testhelper.OutputDirectoryPatch;


public class ConfigurationMethodOrderingTest {
  public static List LOG;
  
  /**
   * @testng.test
   */
  public void checkConfOrderInInheritanceWithNoOverrides() {
    LOG= new ArrayList();
    TestNG tng= new TestNG();
    tng.setOutputDirectory(OutputDirectoryPatch.getOutputDirectory());
    tng.setTestClasses(new Class[] {NonOverriddingChild.class});
    tng.setVerbose(1);
    tng.run();
    System.out.println("[[NonOverriddingChild]]:" + LOG.toString());
    
    assertIsBefore(LOG, "parentBeforeTestClass", "childBeforeTestClass");
    assertIsBefore(LOG, "parentInheritBeforeTestClass", "childBeforeTestClass");
    assertIsBefore(LOG, "parentBeforeTestMethod", "childBeforeTestMethod");
    assertIsBefore(LOG, "parentInheritBeforeTestMethod", "childBeforeTestMethod");
    assertIsAfter(LOG, "parentAfterTestMethod", "childAfterTestMethod");
    assertIsAfter(LOG, "parentInheritAfterTestMethod", "childAfterTestMethod");
    assertIsAfter(LOG, "parentInheritAfterTestClass", "childAfterTestClass");
    assertIsAfter(LOG, "parentAfterTestClass", "childAfterTestClass");
  }
  
  /**
   * @testng.test
   */
  public void checkConfOrderInInheritanceWithSimpleOverrides() {
    LOG= new ArrayList();
    TestNG tng= new TestNG();
    tng.setOutputDirectory(OutputDirectoryPatch.getOutputDirectory());
    tng.setTestClasses(new Class[] {SimpleOverriddingChild.class});
    tng.setVerbose(1);
    tng.run();
    System.out.println("[[SimpleOverriddingChild]]:" + LOG.toString());
    
    assertIsBefore(LOG, "parentBeforeTestClass", "childBeforeTestClass");
    assertIsBefore(LOG, "parentBeforeTestClass", "childInheritBeforeTestClass");
    assertIsBefore(LOG, "parentBeforeTestMethod", "childBeforeTestMethod");
    assertIsBefore(LOG, "parentBeforeTestMethod", "childInheritBeforeTestMethod");
    assertIsAfter(LOG, "parentAfterTestMethod", "childAfterTestMethod");
    assertIsAfter(LOG, "parentAfterTestMethod", "childInheritAfterTestMethod");
    assertIsAfter(LOG, "parentAfterTestClass", "childInheritAfterTestClass");
    assertIsAfter(LOG, "parentAfterTestClass", "childAfterTestClass");
  }
  
  /**
   * @testng.test
   */
  public void checkConfOrderInInheritanceWithFullOverrides() {
    LOG= new ArrayList();
    TestNG tng= new TestNG();
    tng.setOutputDirectory(OutputDirectoryPatch.getOutputDirectory());
    tng.setTestClasses(new Class[] {OverriddingChild.class});
    tng.setVerbose(1);
    tng.run();
    System.out.println("[[OverriddingChild]]:" + LOG.toString());
    
    assertIsBefore(LOG, "parentBeforeTestClass", "childBeforeTestClass");
    assertIsBefore(LOG, "parentBeforeTestClass", "childInheritBeforeTestClass");
    assertIsBefore(LOG, "parentBeforeTestMethod", "childBeforeTestMethod");
    assertIsBefore(LOG, "parentBeforeTestMethod", "childInheritBeforeTestMethod");
    assertIsAfter(LOG, "parentAfterTestMethod", "childAfterTestMethod");
    assertIsAfter(LOG, "parentAfterTestMethod", "childInheritAfterTestMethod");
    assertIsAfter(LOG, "parentAfterTestClass", "childInheritAfterTestClass");
    assertIsAfter(LOG, "parentAfterTestClass", "childAfterTestClass");
  }
  
  private void assertIsBefore(List list, final String first, final String second) {
    int firstIdx = list.indexOf(first);
    int secondIdx = list.indexOf(second);
    
    Assert.assertTrue(firstIdx != -1, "<" + first + "> should be in the list");
    Assert.assertTrue(secondIdx != -1, "<" + second + "> should be in the list");
    Assert.assertTrue(firstIdx < secondIdx, "<" + first + "> should be before <" + second + ">");
  }
  
  private void assertIsAfter(List list, final String second, final String first) {
    assertIsBefore(list, first, second);
  }
}
