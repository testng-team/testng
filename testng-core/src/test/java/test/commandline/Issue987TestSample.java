package test.commandline;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.testng.annotations.Test;

public class Issue987TestSample {
  public static Map<Long, String> maps = new ConcurrentHashMap<>();

  @Test
  public void method1() {
    maps.put(Thread.currentThread().getId(), "method1");
  }

  @Test
  public void method2() {
    maps.put(Thread.currentThread().getId(), "method2");
  }
}
