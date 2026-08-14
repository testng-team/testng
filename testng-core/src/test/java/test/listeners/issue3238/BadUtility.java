package test.listeners.issue3238;

public class BadUtility {

  private static final int counter = evaluate();

  private static int evaluate() {
    throw new RuntimeException("Failed on purpose");
  }

  public static void doNothing() {}
}
