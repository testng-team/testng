package test.guice.issue2427.modules;

import com.google.inject.AbstractModule;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

abstract class TestAbstractModule extends AbstractModule {
  private final AtomicInteger counter;

  protected TestAbstractModule(AtomicInteger counter) {
    this.counter = counter;
  }

  @Override
  protected void configure() {
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
