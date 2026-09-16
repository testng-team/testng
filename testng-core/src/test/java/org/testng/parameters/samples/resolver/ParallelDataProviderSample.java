package org.testng.parameters.samples.resolver;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Three rows run in parallel; each records the thread its body ran on, keyed by its resolved value.
 */
public class ParallelDataProviderSample {

  public static final Map<CustomObject, Thread> INVOKED_ON = new ConcurrentHashMap<>();

  @DataProvider(name = "dp", parallel = true)
  public Object[][] dp() {
    return new Object[][] {{"one"}, {"two"}, {"three"}};
  }

  @Test(dataProvider = "dp")
  public void test(@FromResolver CustomObject custom, String row) {
    INVOKED_ON.put(custom, Thread.currentThread());
    ParameterRecorder.record("test", custom, row);
  }
}
