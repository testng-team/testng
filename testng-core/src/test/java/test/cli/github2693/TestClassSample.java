package test.cli.github2693;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestClassSample {
  public static Set<Long> threads = ConcurrentHashMap.newKeySet();

  @Test(dataProvider = "numbers")
  public void test(Integer i) {
    threads.add(Thread.currentThread().getId());
  }

  @DataProvider(parallel = true)
  public Object[][] numbers() {
    return IntStream.range(1, 20)
        .boxed()
        .map(each -> new Integer[] {each})
        .toArray(Integer[][]::new);
  }
}
