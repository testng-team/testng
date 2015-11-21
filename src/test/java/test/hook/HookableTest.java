package test.hook;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import test.BaseTest;

/**
 * Because IHookable and IConfigurable are global, it's safer to run them in a
 * sub-TestNG object, otherwise they will be run for your entire test suite...
 *
 * @author cbeust
 */
public class HookableTest extends BaseTest {

  @BeforeMethod
  public void bm() {
    HookSuccessTest.m_hook = false;
    HookSuccessTest.m_testWasRun = false;
    HookSuccessTest.m_parameter = null;
    HookSuccess599Test.m_hook = false;
    HookSuccess599Test.m_testWasRun = false;
    HookSuccess599Test.m_parameter = null;
    HookFailureTest.m_hook = false;
    HookFailureTest.m_testWasRun = false;
    HookListener.m_hook = false;
    BaseConfigurable.m_hookCount = 0;
    BaseConfigurable.m_bc = false;
    BaseConfigurable.m_bm = false;
    BaseConfigurable.m_bs = false;
    BaseConfigurable.m_bt = false;
    BaseConfigurable.m_methodName = null;
    ConfigurableListener.m_hookCount = 0;
    ConfigurableListener.m_methodName = null;
    ConfigurableSuccessWithListenerTest.m_bc = false;
    ConfigurableSuccessWithListenerTest.m_bm = false;
  }

  @Test
  public void hookSuccess() {
    addClass(HookSuccessTest.class);
    run();

    verifyTests("Passed", new String[] { "verify" }, getPassedTests());
    Assert.assertTrue(HookSuccessTest.m_hook);
    Assert.assertTrue(HookSuccessTest.m_testWasRun);
    Assert.assertEquals(HookSuccessTest.m_parameter, "foo");
  }

  @Test(description = "https://github.com/cbeust/testng/issues/599")
  public void issue599() {
    addClass(HookSuccess599Test.class);
    run();

    verifyTests("Passed", new String[] { "verify" }, getPassedTests());
    Assert.assertTrue(HookSuccess599Test.m_hook);
    Assert.assertTrue(HookSuccess599Test.m_testWasRun);
    Assert.assertEquals(HookSuccess599Test.m_parameter, "foo");
  }

  @Test(description = "https://github.com/cbeust/testng/pull/862")
  public void issue862() {
    addClass(HookSuccess862Test.class);
    run();

    verifyTests("Passed", new String[] { "verify" }, getPassedTests());
  }

  @Test
  public void hookSuccessWithListener() {
    addClass(HookSuccessWithListenerTest.class);
    run();

    verifyTests("Passed", new String[] { "verify" }, getPassedTests());
    Assert.assertTrue(HookListener.m_hook);
  }

  @Test
  public void hookFailure() {
    addClass(HookFailureTest.class);
    run();

    // To investigate: TestNG still thinks the test passed since it can't know whether
    // the hook ended up invoking the test or not.
//    verifyTests("Passed", new String[] { }, getPassedTests());
    Assert.assertTrue(HookFailureTest.m_hook);
    Assert.assertFalse(HookFailureTest.m_testWasRun);
  }

  @Test
  public void configurableSuccess() {
    addClass(ConfigurableSuccessTest.class);
    run();

    Assert.assertEquals(BaseConfigurable.m_hookCount, 4);
    Assert.assertTrue(BaseConfigurable.m_bc);
    Assert.assertTrue(BaseConfigurable.m_bm);
    Assert.assertTrue(BaseConfigurable.m_bs);
    Assert.assertTrue(BaseConfigurable.m_bt);
    Assert.assertEquals(BaseConfigurable.m_methodName, "hookWasRun");
  }

  @Test
  public void configurableSuccessWithListener() {
    addClass(ConfigurableSuccessWithListenerTest.class);
    run();

    Assert.assertEquals(ConfigurableListener.m_hookCount, 4);
    Assert.assertTrue(ConfigurableSuccessWithListenerTest.m_bs);
    Assert.assertTrue(ConfigurableSuccessWithListenerTest.m_bt);
    Assert.assertTrue(ConfigurableSuccessWithListenerTest.m_bc);
    Assert.assertTrue(ConfigurableSuccessWithListenerTest.m_bm);
    Assert.assertEquals(ConfigurableListener.m_methodName, "hookWasRun");
  }

  @Test
  public void configurableFailure() {
    addClass(ConfigurableFailureTest.class);
    run();

    Assert.assertEquals(BaseConfigurable.m_hookCount, 4);
    Assert.assertFalse(BaseConfigurable.m_bc);
    Assert.assertFalse(BaseConfigurable.m_bm);
    Assert.assertFalse(BaseConfigurable.m_bs);
    Assert.assertFalse(BaseConfigurable.m_bt);
  }

}
