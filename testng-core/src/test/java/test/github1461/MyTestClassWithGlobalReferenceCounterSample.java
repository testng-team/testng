package test.github1461;

import org.testng.annotations.Test;
import org.testng.log4testng.Logger;

/** we create a test NG class here, which has a global counter, counting all instances. */
public class MyTestClassWithGlobalReferenceCounterSample {
  private static final Logger log =
      Logger.getLogger(MyTestClassWithGlobalReferenceCounterSample.class);

  /** global counter that keeps track on how many objects are currently on the heap */
  public static int currentNumberOfMyTestObjects;

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
}
