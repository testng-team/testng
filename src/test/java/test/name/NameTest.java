package test.name;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

public class NameTest extends SimpleBaseTest {

  @Test
  public void itestTest() {
    TestNG tng = create(SimpleITestSample.class);
    TestListenerAdapter adapter = new TestListenerAdapter();
    tng.addListener(adapter);

    tng.run();

    Assert.assertTrue(adapter.getFailedTests().isEmpty());
    Assert.assertTrue(adapter.getSkippedTests().isEmpty());
    Assert.assertEquals(adapter.getPassedTests().size(), 1);
    ITestResult result = adapter.getPassedTests().get(0);
    Assert.assertEquals(result.getMethod().getMethodName(), "test");
    Assert.assertEquals(result.getName(), "NAME");
    Assert.assertEquals(result.getTestName(), "NAME");
  }

  @Test
  public void itestTestWithXml() {
    TestNG tng = createTests("suite", SimpleITestSample.class);
    TestListenerAdapter adapter = new TestListenerAdapter();
    tng.addListener(adapter);

    tng.run();

    Assert.assertTrue(adapter.getFailedTests().isEmpty());
    Assert.assertTrue(adapter.getSkippedTests().isEmpty());
    Assert.assertEquals(adapter.getPassedTests().size(), 1);
    ITestResult result = adapter.getPassedTests().get(0);
    Assert.assertEquals(result.getMethod().getMethodName(), "test");
    Assert.assertEquals(result.getName(), "NAME");
    Assert.assertEquals(result.getTestName(), "NAME");
  }

  @Test
  public void testNameTest() {
    TestNG tng = create(NameSample.class);
    TestListenerAdapter adapter = new TestListenerAdapter();
    tng.addListener(adapter);

    tng.run();

    Assert.assertTrue(adapter.getFailedTests().isEmpty());
    Assert.assertTrue(adapter.getSkippedTests().isEmpty());
    Assert.assertEquals(adapter.getPassedTests().size(), 1);
    ITestResult result = adapter.getPassedTests().get(0);
    Assert.assertEquals(result.getMethod().getMethodName(), "test");
    Assert.assertEquals(result.getName(), "NAME");
    Assert.assertEquals(result.getTestName(), "NAME");
  }

  @Test
  public void testNameTestWithXml() {
    TestNG tng = createTests("suite", NameSample.class);
    TestListenerAdapter adapter = new TestListenerAdapter();
    tng.addListener(adapter);

    tng.run();

    Assert.assertTrue(adapter.getFailedTests().isEmpty());
    Assert.assertTrue(adapter.getSkippedTests().isEmpty());
    Assert.assertEquals(adapter.getPassedTests().size(), 1);
    ITestResult result = adapter.getPassedTests().get(0);
    Assert.assertEquals(result.getMethod().getMethodName(), "test");
    Assert.assertEquals(result.getName(), "NAME");
    Assert.assertEquals(result.getTestName(), "NAME");
  }

  @Test
  public void noNameTest() {
    TestNG tng = create(NoNameSample.class);
    TestListenerAdapter adapter = new TestListenerAdapter();
    tng.addListener(adapter);

    tng.run();

    Assert.assertTrue(adapter.getFailedTests().isEmpty());
    Assert.assertTrue(adapter.getSkippedTests().isEmpty());
    Assert.assertEquals(adapter.getPassedTests().size(), 1);
    ITestResult result = adapter.getPassedTests().get(0);
    Assert.assertEquals(result.getMethod().getMethodName(), "test");
    Assert.assertEquals(result.getName(), "test");
    Assert.assertNull(result.getTestName());
  }

  @Test
  public void noNameTestWithXml() {
    TestNG tng = createTests("suite", NoNameSample.class);
    TestListenerAdapter adapter = new TestListenerAdapter();
    tng.addListener(adapter);

    tng.run();

    Assert.assertTrue(adapter.getFailedTests().isEmpty());
    Assert.assertTrue(adapter.getSkippedTests().isEmpty());
    Assert.assertEquals(adapter.getPassedTests().size(), 1);
    ITestResult result = adapter.getPassedTests().get(0);
    Assert.assertEquals(result.getMethod().getMethodName(), "test");
    Assert.assertEquals(result.getName(), "test");
    Assert.assertNull(result.getTestName());
  }

  @Test
  public void complexITestTest() {
    TestNG tng = create(ITestSample.class);
    TestListenerAdapter adapter = new TestListenerAdapter();
    tng.addListener(adapter);

    tng.run();

    Assert.assertTrue(adapter.getFailedTests().isEmpty());
    Assert.assertTrue(adapter.getSkippedTests().isEmpty());
    Assert.assertEquals(adapter.getPassedTests().size(), 5);
    List<String> testNames = new ArrayList<>(Arrays.asList("test1", "test2", "test3", "test4", "test5"));
    for (ITestResult testResult : adapter.getPassedTests()) {
      Assert.assertTrue(testNames.remove(testResult.getName()),
                        "Duplicate test names " + getNames(adapter.getPassedTests()));
    }
    Assert.assertEquals(testNames, Collections.emptyList());
  }

  private static List<String> getNames(List<ITestResult> results) {
    List<String> names = new ArrayList<>(results.size());
    for (ITestResult result : results) {
      names.add(result.getName());
    }
    return names;
  }

  @Test(description = "GITHUB-922: ITestResult doesn't contain name if a class has @Test")
  public void testOnClassFromReporter() {
    TestNG tng = create(TestOnClassSample.class);
    TestOnClassListener listener = new TestOnClassListener();
    tng.addListener(listener);

    tng.run();

    Assert.assertEquals(listener.getNames().size(), 1);
    Assert.assertEquals(listener.getNames().get(0), "test");
    
    // testName should be ignored if not specified
    Assert.assertEquals(listener.getTestNames().size(), 1);
    Assert.assertNull(listener.getTestNames().get(0));
  }
  
  @Test
  public void blankNameTest() {
	    TestNG tng = create(BlankNameSample.class);
	    TestListenerAdapter adapter = new TestListenerAdapter();
	    tng.addListener(adapter);

	    tng.run();

	    Assert.assertTrue(adapter.getFailedTests().isEmpty());
	    Assert.assertTrue(adapter.getSkippedTests().isEmpty());
	    Assert.assertEquals(adapter.getPassedTests().size(), 1);
	    ITestResult result = adapter.getPassedTests().get(0);
	    Assert.assertEquals(result.getMethod().getMethodName(), "test");
	    Assert.assertEquals(result.getName(), "");
	    Assert.assertEquals(result.getTestName(), "");
  }
  
  @Test
  public void blankNameTestWithXml() {
	    TestNG tng = createTests("suite", BlankNameSample.class);
	    TestListenerAdapter adapter = new TestListenerAdapter();
	    tng.addListener(adapter);

	    tng.run();

	    Assert.assertTrue(adapter.getFailedTests().isEmpty());
	    Assert.assertTrue(adapter.getSkippedTests().isEmpty());
	    Assert.assertEquals(adapter.getPassedTests().size(), 1);
	    ITestResult result = adapter.getPassedTests().get(0);
	    Assert.assertEquals(result.getMethod().getMethodName(), "test");
	    Assert.assertEquals(result.getName(), "");
	    Assert.assertEquals(result.getTestName(), "");
  }
}
