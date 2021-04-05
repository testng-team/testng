package test.tmp;

import org.testng.annotations.Test;

@Test
public class StaticInitializerTest {

    static {
      foo();
    }

    public void testMe() {
        System.err.println("**** testMe ****");
    }

    private static void foo() {
      throw new RuntimeException("FAILING");
    }
}