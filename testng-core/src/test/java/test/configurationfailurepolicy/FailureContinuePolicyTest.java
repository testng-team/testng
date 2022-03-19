package test.configurationfailurepolicy;

import static org.testng.Assert.assertEquals;
import static test.SimpleBaseTest.getPathToResource;

import org.testng.ITestContext;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import test.configurationfailurepolicy.issue2731.ConfigFailTestSample;
import testhelper.OutputDirectoryPatch;

public class FailureContinuePolicyTest {
  // only if this is run from an xml file that sets this on the suite
  @BeforeClass(enabled = false)
  public void setupClass(ITestContext testContext) {
    assertEquals(
        testContext.getSuite().getXmlSuite().getConfigFailurePolicy(),
        XmlSuite.FailurePolicy.CONTINUE);
  }

  @DataProvider(name = "dp")
  public Object[][] getData() {
    return new Object[][] {
      // params - confFail, confSkip, skippedTests
      new Object[] {new Class[] {ClassWithFailedBeforeClassMethod.class}, 1, 0, 0},
      new Object[] {new Class[] {ClassWithFailedBeforeClassMethodAndAfterClass.class}, 1, 0, 0},
      new Object[] {new Class[] {ClassWithFailedBeforeMethodAndMultipleTests.class}, 2, 0, 0},
      new Object[] {
        new Class[] {ClassWithFailedBeforeClassMethodAndBeforeMethodAfterMethodAfterClass.class},
        1,
        0,
        0
      },
      new Object[] {new Class[] {ClassWithFailedBeforeMethodAndMultipleInvocations.class}, 4, 0, 0},
      new Object[] {new Class[] {ExtendsClassWithFailedBeforeMethod.class}, 2, 0, 0},
      new Object[] {new Class[] {ExtendsClassWithFailedBeforeClassMethod.class}, 1, 0, 0},
      new Object[] {
        new Class[] {
          ClassWithFailedBeforeClassMethod.class, ExtendsClassWithFailedBeforeClassMethod.class
        },
        2,
        0,
        0
      },
      new Object[] {new Class[] {ClassWithSkippingBeforeMethod.class}, 0, 1, 0},
      new Object[] {new Class[] {FactoryClassWithFailedBeforeMethod.class}, 2, 0, 0},
      new Object[] {
        new Class[] {FactoryClassWithFailedBeforeMethodAndMultipleInvocations.class}, 8, 0, 0
      },
      new Object[] {new Class[] {FactoryClassWithFailedBeforeClassMethod.class}, 2, 0, 0},
      new Object[] {new Class[] {ConfigFailTestSample.class}, 4, 0, 0}
    };
  }

  @Test(dataProvider = "dp")
  public void confFailureTest(
      Class[] classesUnderTest,
      int configurationFailures,
      int configurationSkips,
      int skippedTests) {

    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG testng = new TestNG();
    testng.setOutputDirectory(OutputDirectoryPatch.getOutputDirectory());
    testng.setTestClasses(classesUnderTest);
    testng.addListener(tla);
    testng.setConfigFailurePolicy(XmlSuite.FailurePolicy.CONTINUE);
    testng.run();

    verify(tla, configurationFailures, configurationSkips, skippedTests);
  }

  @Test
  public void confFailureTestInvolvingGroups() {
    Class[] classesUnderTest =
        new Class[] {ClassWithFailedBeforeClassMethodAndBeforeGroupsAfterClassAfterGroups.class};

    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG testng = new TestNG();
    testng.setOutputDirectory(OutputDirectoryPatch.getOutputDirectory());
    testng.setTestClasses(classesUnderTest);
    testng.addListener(tla);
    testng.setConfigFailurePolicy(XmlSuite.FailurePolicy.CONTINUE);
    testng.setGroups("group1");
    testng.run();
    verify(tla, 1, 0, 0);
  }

  @Test
  public void commandLineTest_policyAsSkip() {
    String[] argv =
        new String[] {
          "-log",
          "0",
          "-d",
          OutputDirectoryPatch.getOutputDirectory(),
          "-configfailurepolicy",
          "skip",
          "-testclass",
          ClassWithFailedBeforeMethodAndMultipleTests.class.getCanonicalName()
        };
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG.privateMain(argv, tla);

    verify(tla, 1, 1, 2);
  }

  @Test
  public void commandLineTest_policyAsContinue() {
    String[] argv =
        new String[] {
          "-log",
          "0",
          "-d",
          OutputDirectoryPatch.getOutputDirectory(),
          "-configfailurepolicy",
          "continue",
          "-testclass",
          ClassWithFailedBeforeMethodAndMultipleTests.class.getCanonicalName()
        };
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG.privateMain(argv, tla);

    verify(tla, 2, 0, 0);
  }

  @Test
  public void commandLineTestWithXMLFile_policyAsSkip() {
    String[] argv =
        new String[] {
          "-log",
          "0",
          "-d",
          OutputDirectoryPatch.getOutputDirectory(),
          "-configfailurepolicy",
          "skip",
          getPathToResource("testng-configfailure.xml")
        };
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG.privateMain(argv, tla);

    verify(tla, 1, 1, 2);
  }

  @Test
  public void commandLineTestWithXMLFile_policyAsContinue() {
    String[] argv =
        new String[] {
          "-log",
          "0",
          "-d",
          OutputDirectoryPatch.getOutputDirectory(),
          "-configfailurepolicy",
          "continue",
          getPathToResource("testng-configfailure.xml")
        };
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG.privateMain(argv, tla);

    verify(tla, 2, 0, 0);
  }

  private void verify(
      TestListenerAdapter tla,
      int configurationFailures,
      int configurationSkips,
      int skippedTests) {
    assertEquals(
        tla.getConfigurationFailures().size(),
        configurationFailures,
        "wrong number of configuration failures");
    assertEquals(
        tla.getConfigurationSkips().size(),
        configurationSkips,
        "wrong number of configuration skips");
    assertEquals(tla.getSkippedTests().size(), skippedTests, "wrong number of skipped tests");
  }
}
