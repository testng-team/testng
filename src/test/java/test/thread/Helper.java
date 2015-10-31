package test.thread;

import java.util.HashMap;
import java.util.Map;

public class Helper {
  private static Map<String, Map<Long, Long>> m_maps = new HashMap<>();

  public static Map<Long, Long> getMap(String className) {
    synchronized(m_maps) {
      Map<Long, Long> result = m_maps.get(className);
      if (result == null) {
        // TODO a synchronizedMap will break MultiThreadedDependentSampleTest
        // a not synchronizedMap will __sometimes__ break ParallelITestTest
        //result = Collections.synchronizedMap(new HashMap<Long, Long>());
        result = new HashMap<>();
        m_maps.put(className, result);
      }
      return result;
    }
//    System.out.println("Putting class:" + className + " result:" + result);

  }

  public static void reset() {
    m_maps = new HashMap<>();
  }
}
