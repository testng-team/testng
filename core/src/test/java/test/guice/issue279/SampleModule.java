package test.guice.issue279;

import com.google.inject.AbstractModule;

public class SampleModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(Greeter.class).to(TextGreeter.class).asEagerSingleton();
    bind(Vehicle.class).to(Car.class).asEagerSingleton();
  }
}
