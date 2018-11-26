package org.testng.internal;

import org.testng.ITestNGMethod;

import java.util.Comparator;

/** Helps determine how should {@link ITestNGMethod} be ordered by TestNG. */
public final class Systematiser {

  private Systematiser() {
    // Utility class. Defeat instantiation.
  }

  /**
   * @return - A {@link Comparator} that helps TestNG sort {@link ITestNGMethod}s in a specific
   *     order. Currently the following two orders are supported : <br>
   *     <ol>
   *       <li>Based on the name of methods
   *       <li>Based on the <code>toString()</code> implementation that resides within the test
   *           class
   *     </ol>
   */
  public static Comparator<ITestNGMethod> getComparator() {
    Comparator<ITestNGMethod> comparator;
    String text = RuntimeBehavior.orderMethodsBasedOn();

    Order order = Order.parse(text);
    switch (order) {
      case METHOD_NAMES:
        comparator =
            new Comparator<ITestNGMethod>() {
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
            };
        break;

      case NONE:
        // Disables sorting by providing a dummy comparator which always regards two elements as
        // equal.
        comparator =
            new Comparator<ITestNGMethod>() {
              @Override
              public int compare(ITestNGMethod o1, ITestNGMethod o2) {
                return 0;
              }

              @Override
              public String toString() {
                return "No_Sorting";
              }
            };
        break;

      case INSTANCES:
      default:
        comparator =
            new Comparator<ITestNGMethod>() {
              @Override
              public int compare(ITestNGMethod o1, ITestNGMethod o2) {
                return o1.toString().compareTo(o2.toString());
              }

              @Override
              public String toString() {
                return "Instance_Names";
              }
            };
    }

    return comparator;
  }

  enum Order {
    METHOD_NAMES("methods"),
    INSTANCES("instances"),
    NONE("none");

    Order(String value) {
      this.value = value;
    }

    private String value;

    public String getValue() {
      return value;
    }

    public static Order parse(String value) {
      if (value == null || value.trim().isEmpty()) {
        return INSTANCES;
      }
      for (Order each : values()) {
        if (each.getValue().equalsIgnoreCase(value)) {
          return each;
        }
      }
      return INSTANCES;
    }
  }
}
