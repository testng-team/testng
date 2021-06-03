package test.github1461;

import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.log4testng.Logger;

/**
 * This class reproduces a memory leak problem when running TestNG tests. The same (memory behavior
 * will be shown when running this test as normal TestNG test (e.g. using Intellij)
 *
 * <p>See https://github.com/cbeust/testng/issues/1461
 */
public class MemoryLeakTestNg {
  private static final Logger log = Logger.getLogger(MemoryLeakTestNg.class);

  @Test(timeOut = 10_000)
  public void testMemoryLeak() throws Exception {

    // we run the test programmatically
    runTest();
    // lets wait for garbage collection
    waitForAllObjectsDestructed();
  }

  private static void waitForAllObjectsDestructed() throws InterruptedException {
    while (true) {
      log.debug("waiting for clean up...");
      // enforce a full gc
      System.gc();

      // check if there are still instances of our test class
      if (MyTestClassWithGlobalReferenceCounterSample.currentNumberOfMyTestObjects == 0) {
        // if we reach this point, all test instances are gone,
        // ... however this never happens ...
        break;
      }
      // let's wait 1 seconds and try again ...
      Thread.sleep(1_000);
      log.debug(
          "["
              + MyTestClassWithGlobalReferenceCounterSample.currentNumberOfMyTestObjects
              + "] test object(s) still exist.");
    }
  }

  private static void runTest() {

    // create TestNG class
    TestNG testng =
        new TestNG() {
          @Override
          @SuppressWarnings("deprecation")
          protected void finalize() {
            // it seems that this object will never be finalized !!!
            log.debug("TestNG finalized");
          }
        };

    // and set a test (which also will never be finalized ... see later)
    testng.setTestClasses(
        new Class[] {
          MyTestClassWithGlobalReferenceCounterSample.class,
        });

    // lets run the test
    testng.run();

    // At this point the test run through and we expect both instances
    // - testng and
    // - the test object of type (MyTest)
    // will be garbage collected when leaving this method
    // ...
  }
}
