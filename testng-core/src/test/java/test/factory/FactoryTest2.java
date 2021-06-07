package test.factory;

import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.Test;

public class FactoryTest2 {
  private static List<Integer> numbers = new ArrayList<>();
  private int number;

  public FactoryTest2() {
    throw new RuntimeException("Shouldn't be invoked");
  }

  public static List<Integer> getNumbers() {
    return numbers;
  }

  public FactoryTest2(int n) {
    number = n;
  }

  @Test(groups = {"first"})
  public void testInt() {
    Integer n = number;
    numbers.add(n);
  }

  @Override
  public String toString() {
    return "[FactoryTest2 " + number + "]";
  }
}
