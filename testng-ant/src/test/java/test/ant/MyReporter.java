package test.ant;

import org.testng.IReporter;

public class MyReporter implements IReporter {

  public static String expectedFilter;
  public static boolean expectedFiltering;

  public void setMethodFilter(String filter) {
    if (!filter.equals(expectedFilter)) {
      throw new IllegalArgumentException("Expect filter: " + expectedFilter);
    }
  }

 
  }
