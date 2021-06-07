package test.factory;

import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.Test;

/** This class is created by FactoryWithInstanceInfo2Sample */
public class FactoryWithInstanceInfo2Sample {

  private static List<Integer> numbers = new ArrayList<>();
  private int number;

  public FactoryWithInstanceInfo2Sample() {
    throw new RuntimeException("Shouldn't be invoked");
  }

  public static List<Integer> getNumbers() {
    return numbers;
  }

  public FactoryWithInstanceInfo2Sample(int n) {
    number = n;
  }

  @Test(groups = {"first"})
  public void testInt() {
    numbers.add(number);
  }
}
