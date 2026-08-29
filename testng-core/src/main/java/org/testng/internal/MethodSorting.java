package org.testng.internal;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.testng.IFactoryInstance;
import org.testng.ITestNGMethod;

public enum MethodSorting implements Comparator<ITestNGMethod> {
  METHOD_NAMES("methods") {
    @Override
    public int compare(ITestNGMethod o1, ITestNGMethod o2) {
      String n1 = o1.getMethodName();
      String n2 = o2.getMethodName();
      return n1.compareTo(n2);
    }

    @Override
    public String toString() {
      return "Method_Names";
    }
  },
  INSTANCES("instances") {
    @Override
    public int compare(ITestNGMethod o1, ITestNGMethod o2) {
      return BY_INSTANCE.compare(o1, o2);
    }

    @Override
    public String toString() {
      return "Instance_Names";
    }
  },
  NONE("none") {
    // Comparator fixes the signature, and NONE compares nothing.
    @Override
    @SuppressWarnings("unused")
    public int compare(ITestNGMethod o1, ITestNGMethod o2) {
      return 0;
    }

    @Override
    public String toString() {
      return "No_Sorting";
    }
  };

  /**
   * The leading key of every ordering that honours a priority: the lower value comes first.
   *
   * <p>It is shared rather than restated because {@link
   * org.testng.internal.MethodHelper#collectAndOrderMethods} composes it in front of whichever
   * ordering is in force when it collects configuration methods, so that a configuration priority
   * counts under {@link #METHOD_NAMES} and {@link #NONE} too.
   */
  static final Comparator<ITestNGMethod> BY_PRIORITY =
      Comparator.comparingInt(ITestNGMethod::getPriority);

  /**
   * The order {@link #INSTANCES} applies, held once rather than rebuilt on every comparison: a
   * comparator chain allocates one wrapper per stage, and a sort asks for one comparison per pair.
   *
   * <p>It lives on the enum rather than inside the {@code INSTANCES} body because a constant body
   * is an anonymous class, which cannot hold a static member before Java 16. Only {@link
   * #compare(ITestNGMethod, ITestNGMethod)} reads it, so the constants being initialised before the
   * static fields does not matter.
   */
  private static final Comparator<ITestNGMethod> BY_INSTANCE =
      BY_PRIORITY
          .thenComparing(method -> method.getRealClass().getName())
          .thenComparing(ITestNGMethod::getMethodName)
          .thenComparing(Object::toString)
          .thenComparing(
              method ->
                  method
                      .getFactoryInstance()
                      .map(IFactoryInstance::getParameters)
                      .map(Arrays::toString)
                      .orElse(""))
          .thenComparing(MethodSorting::objectEquality);

  private static int objectEquality(ITestNGMethod a, ITestNGMethod b) {
    // Use the method's own per-instance id rather than its (possibly lazy) instance, so that
    // sorting never forces a lazy @Factory instance to be created during collection.
    Object one = IInstanceIdentity.getInstanceId(a);
    Object two = IInstanceIdentity.getInstanceId(b);
    // getInstanceId answers the UUID for an identity aware method and the method itself
    // otherwise, so the test belongs on what came back. Testing the inputs for
    // IInstanceIdentity, as this did, could never hold: a UUID is not one, and a method that
    // is one never reaches this branch as itself.
    if (one instanceof UUID && two instanceof UUID) {
      return ((UUID) one).compareTo((UUID) two);
    }
    return Integer.compare(Objects.hashCode(one), Objects.hashCode(two));
  }

  MethodSorting(String value) {
    this.value = value;
  }

  private final String value;

  public static Comparator<ITestNGMethod> basedOn() {
    String text = RuntimeBehavior.orderMethodsBasedOn();
    return MethodSorting.parse(text);
  }

  private static MethodSorting parse(String input) {
    String text = Optional.ofNullable(input).orElse("");
    return Arrays.stream(values())
        .filter(it -> it.value.equalsIgnoreCase(text))
        .findFirst()
        .orElse(INSTANCES);
  }
}
