package test.thread;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;

import test.SimpleBaseTest;

import java.util.Arrays;
import java.util.List;

/**
 * Attempt to make sure that we are truly running methods in parallel. The best I can think
 * of right now is to run the tests a few times in a row and verify the ordering is never
 * the same.
 */
public class TrueParallelTest extends SimpleBaseTest {

  @Test
  public void shouldRunInParallel() {
    boolean success = false;
    for (int i = 0, count = Runtime.getRuntime().availableProcessors() * 4; i < count; i++) {
      XmlSuite s = createXmlSuite("TrueParallel");
      createXmlTest(s, "Test", TrueParallelSampleTest.class.getName());
      TestNG tng = create();
      s.setParallel(XmlSuite.ParallelMode.METHODS);
      tng.setXmlSuites(Arrays.asList(s));
      BaseThreadTest.initThreadLog();
      tng.run();

      // A sequential result will look like "m1 m1 m3 m3 m2 m2 m4 m4 m5 m5". A properly
      // multithreaded result will have at least one non-consecutive different pair:
      // "m1 m1 m3 m2 m4 m4 m2 m3 m5 m5"
      List<String> strings = TrueParallelSampleTest.getStrings();
      boolean ii = isInterleaved(strings);
      success = success || ii;
//      System.out.println(strings + " -> " + ii);
    }
    Assert.assertTrue(success, "Couldn't find any interleaved test method run");
  }

  private boolean isInterleaved(List<String> strings) {
    for (int i = 0; i < strings.size(); i += 2) {
      if (! strings.get(i).equals(strings.get(i + 1))) {
        return true;
      }
    }
    return false;
  }
}
