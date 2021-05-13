package test.listeners;

import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(SuiteListener2.class)
public class SuiteListenerSample2 {

    @Test
    public void foo(){}
}
