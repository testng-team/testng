package test.listeners;

import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(SuiteListener.class)
public class SuiteListenerSample {

    @Test
    public void foo(){}
}
