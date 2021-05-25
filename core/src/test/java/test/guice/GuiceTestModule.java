package test.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;

public class GuiceTestModule extends AbstractModule {
  private final MyContext myContext;

  @Inject
  GuiceTestModule(MyContext myContext) {
    this.myContext = myContext;
  }

  @Override
  protected void configure() {
    bind(MySession.class).toInstance(myContext.getSession());
  }
}
