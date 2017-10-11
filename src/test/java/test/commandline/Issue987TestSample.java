package test.commandline;

import org.testng.annotations.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
