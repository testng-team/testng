package test.guice;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.google.inject.Inject;

@Test
@Guice(modules = GuiceTestModule.class)
public class GuiceParentModuleTest {
  @Inject
  MySession mySession;
  @Inject
  MyService myService;
  @Inject
  ITestContext context;

  public void testService() {
    Assert.assertNotNull(myService);
    Assert.assertNotNull(mySession);
    myService.serve(mySession);
    Assert.assertNotNull(context);
    Assert.assertEquals(context.getName(), "Guice");
    Assert.assertEquals(context.getSuite().getName(), "parent-module-suite");
  }
}
