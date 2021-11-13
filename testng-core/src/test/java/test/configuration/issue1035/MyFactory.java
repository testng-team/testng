package test.configuration.issue1035;

import static java.util.Collections.newSetFromMap;
import static java.util.Collections.synchronizedSet;

import java.util.IdentityHashMap;
import java.util.Set;
import org.testng.annotations.Factory;

public class MyFactory {
  public static final Set<InvocationTracker> TRACKER =
      synchronizedSet(newSetFromMap(new IdentityHashMap<>()));

  @Factory
  public Object[] instances() {
    return new Object[] {
      new TestclassExample(),
      new TestclassExample(),
      new TestclassExample(),
      new TestclassExample(),
      new TestclassExample()
    };
  }
}
