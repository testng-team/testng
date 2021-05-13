package test.github1461;

import org.testng.annotations.Test;

/** we create a test NG class here, which has a global counter, counting all instances. */
public class MyTestClassWithGlobalReferenceCounterSample {

  /** global counter that keeps track on how many objects are currently on the heap */
  public static int currentNumberOfMyTestObjects = 0;

  public MyTestClassWithGlobalReferenceCounterSample() {
    System.out.println("constructor");
    // increase the counter
    ++currentNumberOfMyTestObjects;
  }

  @Test
  public void aTestMethod1() {
    System.out.println("test method 1");
  }

  @Test
  public void aTestMethod2() {
    System.out.println("test method 2");
  }

  @Override
  protected void finalize() {
    System.out.println("finalize");
    // this will be called when this object is removed from the heap
    --currentNumberOfMyTestObjects;
  }
}
