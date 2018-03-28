package test.listeners.github1735;

import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(LocalExecutionListener.class)
public class TestClassSample {
    @Test
    public void testMethod(){}
}
