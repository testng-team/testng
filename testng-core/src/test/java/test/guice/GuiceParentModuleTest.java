package test.guice;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.inject.Inject;
import org.testng.ITestContext;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

@Test
@Guice(modules = GuiceTestModule.class)
public class GuiceParentModuleTest {
  @Inject MySession mySession;
  @Inject MyService myService;
  @Inject ITestContext context;

  public void testService() {
    assertThat(myService).isNotNull();
    assertThat(mySession).isNotNull();
    myService.serve(mySession);
    assertThat(context).isNotNull();
    assertThat(context.getName()).isEqualTo("Guice");
    assertThat(context.getSuite().getName()).isEqualTo("parent-module-suite");
  }
}
