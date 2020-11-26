package test.guice.issue2427.modules;

import java.util.concurrent.atomic.AtomicInteger;

public class TestParentConfigModule extends TestAbstractModule {
  public static AtomicInteger counter = new AtomicInteger(0);

  public TestParentConfigModule() {
    super(counter);
  }
}
