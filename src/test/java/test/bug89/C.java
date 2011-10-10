package test.bug89;

import org.testng.annotations.Test;

public class C extends B{
	 String threadC;
	
    @Test(enabled=false)
    public void veryImportant() throws InterruptedException {
    	scenario("veryImportant");
    	threadC=""+Thread.currentThread();
    	System.out.println(threadC);
    }
}
