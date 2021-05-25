package test.thread;

import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Github1636Sample {
    private static Map<Long, Boolean> map = new ConcurrentHashMap<>();
    static Set<Long> threads = Collections.newSetFromMap(map);

    @Test
    public void test1() {
        threads.add(Thread.currentThread().getId());
    }

    @Test
    public void test2() {
        threads.add(Thread.currentThread().getId());
    }

    @Test
    public void test3() {
        threads.add(Thread.currentThread().getId());
    }

}
