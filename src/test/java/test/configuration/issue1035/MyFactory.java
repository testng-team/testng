package test.configuration.issue1035;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.testng.annotations.Factory;

public class MyFactory {

  public static final Set<InvocationTracker> TRACKER = Collections
      .newSetFromMap(new ConcurrentHashMap<>());

  @Factory
  public Object[] instances() {
    return new Object[]{
        new TestclassExample(), new TestclassExample(), new TestclassExample(),
        new TestclassExample(), new TestclassExample()
    };
  }
}
