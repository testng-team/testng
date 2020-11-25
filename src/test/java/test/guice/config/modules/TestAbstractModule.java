package test.guice.config.modules;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.inject.AbstractModule;

abstract class TestAbstractModule extends AbstractModule {
  private final AtomicInteger counter;

  protected TestAbstractModule(AtomicInteger counter) {
    this.counter = counter;
  }

  @Override
  protected void configure() {
    System.out.println(String.format("Configuring %s module", this.getClass().getSimpleName()));
    counter.incrementAndGet();
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return obj != null && Objects.equals(getClass(), obj.getClass());
  }
}
