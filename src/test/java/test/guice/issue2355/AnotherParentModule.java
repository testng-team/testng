package test.guice.issue2355;

import com.google.inject.AbstractModule;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Singleton;
import test.guice.issue2343.Person;

public class AnotherParentModule extends AbstractModule {

  private static final AtomicInteger counter = new AtomicInteger(0);

  public AnotherParentModule() {
    counter.incrementAndGet();
  }

  public static int getCounter() {
    return counter.get();
  }

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
