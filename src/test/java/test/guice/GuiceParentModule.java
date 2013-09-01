package test.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

public class GuiceParentModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(MyService.class).toProvider(MyServiceProvider.class);
    bind(MyContext.class).to(MyContextImpl.class).in(Singleton.class);
  }
}
