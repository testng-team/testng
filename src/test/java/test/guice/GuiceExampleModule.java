package test.guice;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Singleton;

public class GuiceExampleModule implements Module {

  @Override
  public void configure(Binder binder) {
    binder.bind(ISingleton.class).to(ExampleSingleton.class).in(Singleton.class);
  }

}
