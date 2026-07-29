package test.listeners.cliwiring;

import org.testng.ITestNGListener;
import org.testng.ListenerComparator;

/** Orders listeners by class name, descending, so the wiring order is observable. */
public class ReverseNameListenerComparator implements ListenerComparator {
  @Override
  public int compare(ITestNGListener first, ITestNGListener second) {
    return second.getClass().getName().compareTo(first.getClass().getName());
  }
}
