package org.testng.v6;

import java.util.ArrayList;
import java.util.List;

public class Lists {

  public static <T> List<T> newArrayList() {
    return new ArrayList<T>();
  }
}
