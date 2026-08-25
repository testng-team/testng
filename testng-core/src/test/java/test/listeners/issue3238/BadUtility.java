package test.listeners.issue3238;

public class BadUtility {

  // Never read on purpose: evaluating it is what makes class initialisation fail, which is
  // the condition issue3238 is about.
  @SuppressWarnings("unused")
  private static final int counter = evaluate();

  private static int evaluate() {
    throw new RuntimeException("Failed on purpose");
  }

  public static void doNothing() {}
}
