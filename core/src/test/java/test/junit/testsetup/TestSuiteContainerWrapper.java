package test.junit.testsetup;

import junit.extensions.TestSetup;
import junit.framework.TestSuite;

public class TestSuiteContainerWrapper extends TestSetup {
  private static Data INSTANCE = null;
  private static TestSuite _test = null;
  private static Class dataImpl = null;

  public TestSuiteContainerWrapper(TestSuite testSuite, Class dataImpl) {
    super(testSuite);
    _test = testSuite;
    TestSuiteContainerWrapper.dataImpl = dataImpl;
  }

  public static Data getData() {
    return INSTANCE;
  }

  @Override
  protected void setUp() throws Exception {
    System.out.println("setup");
    INSTANCE = (Data) dataImpl.newInstance();
  }

  @Override
  protected void tearDown() throws Exception {
    System.out.println("teardown");

    INSTANCE = null;

    System.out.println(_test.countTestCases() + " test cases defined by \""
        + _test.getName() + "\" were executed.");
  }
}
