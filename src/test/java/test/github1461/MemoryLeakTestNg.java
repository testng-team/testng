package test.github1461;

import org.testng.TestNG;
import org.testng.annotations.Test;

/**
 * This class reproduces a memory leak problem when running TestNG tests.
 * The same (memory behavior will be shown when running this test as normal TestNG test (e.g. using Intellij)
 *
 * See https://github.com/cbeust/testng/issues/1461
 */
public class MemoryLeakTestNg {

    public static void main(String[] args) throws Exception {

        // we run the test programmatically
        runTest();
        // lets wait for garbage collection
        waitForAllObjectsDestructed();
    }

    public static void waitForAllObjectsDestructed() throws InterruptedException {
        while (true) {
            System.out.println("waiting for clean up...");
            // enforce a full gc
            System.gc();

            // check if there are still instances of our test class
            if (MyTestClassWithGlobalReferenceCounter.currentNumberOfMyTestObjects == 0) {
                // if we reach this point, all test instances are gone,
                // ... however this never happens ...
                break;
            }
            // let's wait 5 seconds and try again ...
            Thread.sleep(1000);
            System.out.println("[" + MyTestClassWithGlobalReferenceCounter.currentNumberOfMyTestObjects + "] test object(s) still exist.");
        }
    }

    private static void runTest() {

        // create TestNG class
        TestNG testng = new TestNG() {
            @Override
            protected void finalize() throws Throwable {
                // it seems that this object will never be finalized !!!
                System.out.println("TestNG finalized");
            }
        };


        // and set a test (which also will never be finalized ... see later)
        testng.setTestClasses(new Class[]{
            MyTestClassWithGlobalReferenceCounter.class,
        });

        // lets run the test
        testng.run();

        // At this point the test run through and we expect both instances
        // - testng and
        // - the test object of type (MyTest)
        // will be garbage collected when leaving this method
        // ...

    }

    /**
     * we create a test NG class here, which has a global counter, counting all instances.
     */
    public static class MyTestClassWithGlobalReferenceCounter {

        /**
         * global counter that keeps track on how many objects are currently on the heap
         */
        public static int currentNumberOfMyTestObjects = 0;

        public MyTestClassWithGlobalReferenceCounter() {
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
        protected void finalize() throws Throwable {
            System.out.println("finalize");
            // this will be called when this object is removed from the heap
            --currentNumberOfMyTestObjects;
        }
    }
}