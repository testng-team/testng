package test.bug89;

import org.testng.annotations.Test;

public abstract class A {
	static String threadA="";
	
    @Test
    public void someMethodA1() throws InterruptedException {
        scenario("someMethodA1");
        threadA = ""+Thread.currentThread();
    }

    public void scenario(String s) throws InterruptedException {
        System.out.println("START["+Thread.currentThread()+"]: " + s);
        synchronized (s) { 
            s.wait(1000);
        }
        System.out.println("END["+Thread.currentThread()+"]: " + s);
    }
    

}	