package test.listeners.issue2916;

import java.util.Optional;
import org.testng.ITestNGListener;
import org.testng.ListenerComparator;

public class AnnotationBackedListenerComparator implements ListenerComparator {

  @Override
  public int compare(ITestNGListener l1, ITestNGListener l2) {
    int first = getRunOrder(l1);
    int second = getRunOrder(l2);
    return Integer.compare(first, second);
  }

  private static int getRunOrder(ITestNGListener listener) {
    RunOrder runOrder = listener.getClass().getAnnotation(RunOrder.class);
    return Optional.ofNullable(runOrder)
        .map(RunOrder::value)
        .orElse(Integer.MAX_VALUE); // If annotation was not found then return a max value so that
    // the listener can be plugged in to the end.
  }
}
