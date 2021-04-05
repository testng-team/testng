package test.junit;

import junit.framework.TestCase;

/**
 * Base JUnit test case to verify TestNG handles
 * TestCase hierarchies properly.
 *
 * @author mperham
 */
public abstract class BaseTest extends TestCase {

  private static int setUpInvokeCount = 0;
  private static int tearDownInvokeCount = 0;

  public BaseTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
    setUpInvokeCount++;
  }

  @Override
  protected void tearDown() throws Exception {
    tearDownInvokeCount++;
  }

  public abstract void testA();
  public abstract void testB();

  public static int getSetUpInvokeCount() {
    return setUpInvokeCount;
  }

  public static int getTearDownInvokeCount() {
    return tearDownInvokeCount;
  }

}
