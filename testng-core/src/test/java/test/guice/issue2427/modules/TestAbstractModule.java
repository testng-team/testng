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
  // getClass() on purpose, to agree with hashCode() above, which is getClass().hashCode(). Guice
  // deduplicates modules through a hash set, so instanceof here would make two different concrete
  // modules equal while their hash codes stayed different -- equal objects with unequal hash codes,
  // which is the one thing a hash set may not be given.
  @SuppressWarnings("EqualsGetClass")
  public boolean equals(Object obj) {
    return obj != null && Objects.equals(getClass(), obj.getClass());
  }
}
