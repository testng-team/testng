package org.testng.internal.collections;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class Ints {

  private Ints() {}

  public static List<Integer> asList(int... ints) {
    return Arrays.stream(ints).boxed().collect(Collectors.toList());
  }
}
