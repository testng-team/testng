package test.triangle;


/**
 * count the number of times something is called
 * used to check whether certain methods have been called
 */
public class CountCalls {
  private static int numCalls = 0;
  
  public static void resetNumCalls() {
    numCalls = 0;
  }
  
  public static void incr() {
    numCalls++;
  }
  
  public static int getNumCalls() { 
    return numCalls;
  }
}
