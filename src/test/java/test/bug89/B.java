package test.bug89;

import org.testng.annotations.Test;

public abstract class B extends A{
	public static boolean isSameThread=false;
	static String threadB="";
    @Test
    public void someMethodB1 () throws InterruptedException {
        scenario("someMethodB1");
        threadB = ""+Thread.currentThread();
        System.out.println(threadA);
        System.out.println(threadB);
        if(threadA.equalsIgnoreCase(threadB)){
        	isSameThread=true; 
        }
        	System.out.println(isSameThread);
    }
    
}
