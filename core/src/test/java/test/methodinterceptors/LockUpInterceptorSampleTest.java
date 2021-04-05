package test.methodinterceptors;

import org.testng.annotations.Listeners;
import org.testng.annotations.Test;


@Listeners({RemoveAMethodInterceptor.class})
public class LockUpInterceptorSampleTest {

    @Test
    public void one() {
        log("one");
    }
    
    @Test
    public void two() {
        log("two");
    }
    
    @Test
    public void three() {
        log("three");
    }

    private static void log(String s) {
//      System.out.println("[MITest] " + s);
    }
}