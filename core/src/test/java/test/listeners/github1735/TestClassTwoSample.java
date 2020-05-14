package test.listeners.github1735;

import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(LocalExecutionListener.class)
public class TestClassTwoSample {
    @Test
    public void testMethod(){}
}
