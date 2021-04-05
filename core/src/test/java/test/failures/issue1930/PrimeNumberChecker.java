package test.failures.issue1930;

public class PrimeNumberChecker {

  public boolean validate(int number) {
    if (number == 2 || number == 3) {
      return true;
    }
    if (number % 2 == 0) {
      return false;
    }
    int sqrt = (int) Math.sqrt(number) + 1;
    for (int i = 3; i < sqrt; i += 2) {
      if (number % i == 0) {
        return false;
      }
    }
    return true;
  }
}
