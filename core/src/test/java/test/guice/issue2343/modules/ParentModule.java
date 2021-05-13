package test.guice.issue2343.modules;

import com.google.inject.AbstractModule;
import java.util.Objects;
import javax.inject.Singleton;
import test.guice.issue2343.Person;

public class ParentModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(Person.class).in(Singleton.class);
  }

  @Override
  public boolean equals(final Object object) {
    if (object == null) {
      return false;
    }
    return Objects.equals(getClass(), object.getClass());
  }

  @Override
  public int hashCode() {
    return this.getClass().hashCode();
  }
}
