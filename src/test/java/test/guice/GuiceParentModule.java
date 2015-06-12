package test.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.testng.ITestContext;

public class GuiceParentModule extends AbstractModule {

  private final ITestContext context;

  public GuiceParentModule(ITestContext context) {
    this.context = context;
  }

  @Override
  protected void configure() {
    bind(MyService.class).toProvider(MyServiceProvider.class);
    bind(MyContext.class).to(MyContextImpl.class).in(Singleton.class);
    bind(ITestContext.class).toInstance(context);
  }
}
