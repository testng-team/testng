package test.methodselectors;

import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import test.SimpleBaseTest;
import testhelper.OutputDirectoryPatch;

import java.util.ArrayList;
import java.util.List;

public class CommandLineTest extends SimpleBaseTest {

  private String[] argv;
  private TestListenerAdapter tla;
  
  @BeforeMethod
  public void setup() {
    ppp("setup()");
    argv = new String[]{
        "-log", "0",
        "-d", OutputDirectoryPatch.getOutputDirectory(),
        "-testclass", "test.methodselectors.SampleTest",
        "-methodselectors", "",
        "-groups", ""
    };
    tla = new TestListenerAdapter();
  }
  
  @Test
  public void commandLineNegativePriorityAllGroups() {
    ppp("commandLineNegativePriorityAllGroups()");
    argv[7] = "test.methodselectors.AllTestsMethodSelector:-1";
    TestNG.privateMain(argv, tla);
    String[] passed = {
        "test1", "test2", "test3"
    };
    String[] failed = {
    };
    verifyTests("Passed", passed, tla.getPassedTests());
    verifyTests("Failed", failed, tla.getFailedTests());
  }
  
  @Test
  public void commandLineNegativePriorityGroup2() {
    ppp("commandLineNegativePriorityGroup2()");
    argv[7] = "test.methodselectors.Test2MethodSelector:-1";
    TestNG.privateMain(argv, tla);
    String[] passed = {
        "test2"
    };
    String[] failed = {
    };
    verifyTests("Passed", passed, tla.getPassedTests());
    verifyTests("Failed", failed, tla.getFailedTests());
  }
  
  @Test
  public void commandLineLessThanPriorityTest1Test() {
    ppp("commandLineLessThanPriorityTest1Test()");
    argv[7] = "test.methodselectors.Test2MethodSelector:5";
    argv[9] = "test1";
    TestNG.privateMain(argv, tla);
    String[] passed = {
        "test1", "test2"
    };
    String[] failed = {
    };
    verifyTests("Passed", passed, tla.getPassedTests());
    verifyTests("Failed", failed, tla.getFailedTests());
  }
  
  @Test
  public void commandLineGreaterThanPriorityTest1Test2() {
    ppp("commandLineGreaterThanPriorityTest1Test2()");
    argv[7] = "test.methodselectors.Test2MethodSelector:15";
    argv[9] = "test1";
    TestNG.privateMain(argv, tla);
    String[] passed = {
        "test2"
    };
    String[] failed = {
    };
    verifyTests("Passed", passed, tla.getPassedTests());
    verifyTests("Failed", failed, tla.getFailedTests());
  }
  @Test
  public void commandLineLessThanPriorityAllTests() {
    ppp("commandLineLessThanPriorityAllTests()");
    argv[7] = "test.methodselectors.AllTestsMethodSelector:5";
    argv[9] = "test1";
    TestNG.privateMain(argv, tla);
    String[] passed = {
        "test1", "test2", "test3"
    };
    String[] failed = {
    };
    verifyTests("Passed", passed, tla.getPassedTests());
    verifyTests("Failed", failed, tla.getFailedTests());
  }
  
  @Test
  public void commandLineMultipleSelectors() {
    ppp("commandLineMultipleSelectors()");
    argv[7] = "test.methodselectors.NoTestSelector:7,test.methodselectors.Test2MethodSelector:5";
    argv[9] = "test1";
    TestNG.privateMain(argv, tla);
    String[] passed = {
        "test1", "test2"
    };
    String[] failed = {
    };
    verifyTests("Passed", passed, tla.getPassedTests());
    verifyTests("Failed", failed, tla.getFailedTests());
  }
  
  @Test
  public void commandLineNoTest1Selector() {
    ppp("commandLineNoTest1Selector()");
    argv[7] = "test.methodselectors.NoTest1MethodSelector:5";
    TestNG.privateMain(argv, tla);
    String[] passed = {
        "test2", "test3"
    };
    String[] failed = {
    };
    verifyTests("Passed", passed, tla.getPassedTests());
    verifyTests("Failed", failed, tla.getFailedTests());
  }

  @Test
  public void commandLineTestWithXmlFile() {
    ppp("commandLineTestWithXmlFile()");
    argv[4] = argv[5] = "";
    argv[7] = "test.methodselectors.NoTest1MethodSelector:5";
    argv[8] = getPathToResource("testng-methodselectors.xml");
    TestNG.privateMain(argv, tla);
    String[] passed = {
        "test2", "test3"
    };
    String[] failed = {
    };
    verifyTests("Passed", passed, tla.getPassedTests());
    verifyTests("Failed", failed, tla.getFailedTests());
  }
  
  private void verifyTests(String title, String[] expected, List<ITestResult> found) {
    List<String> resultMethods = new ArrayList<String>();
    for( ITestResult result : found ) {
      resultMethods.add( result.getName() );
    }    
    
    Assert.assertEquals(resultMethods.size(), expected.length, "wrong number of " + title + " tests");

    for(String e : expected) {
      Assert.assertTrue(resultMethods.contains(e), "Expected to find method " + e + " in "
          + title + " but didn't find it.");
    }
  }
  
  public static void ppp(String s) {
    //System.out.println("[CommandLineTest] " + s);
  }
}
