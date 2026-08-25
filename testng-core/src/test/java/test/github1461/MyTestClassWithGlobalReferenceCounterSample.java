package test.github1461;

import org.testng.annotations.Test;
import org.testng.log4testng.Logger;

/** we create a test NG class here, which has a global counter, counting all instances. */
public class MyTestClassWithGlobalReferenceCounterSample {
  private static final Logger log =
      Logger.getLogger(MyTestClassWithGlobalReferenceCounterSample.class);

  /** global counter that keeps track on how many objects are currently on the heap */
  public static int currentNumberOfMyTestObjects = 0;

  public MyTestClassWithGlobalReferenceCounterSample() {
    log.debug("constructor");
    // increase the counter
    ++currentNumberOfMyTestObjects;
  }

  @Test
  public void aTestMethod1() {
    log.debug("test method 1");
  }

  @Test
  public void aTestMethod2() {
    log.debug("test method 2");
  }

  // The finalizer is what decrements the counter MemoryLeakTestNg spins on. Without it that
  // loop never reaches zero and the test fails on its timeOut.
  @Override
  @SuppressWarnings({"deprecation", "Finalize"})
  protected void finalize() {
    log.debug("finalize");
    // this will be called when this object is removed from the heap
    --currentNumberOfMyTestObjects;
  }
}
