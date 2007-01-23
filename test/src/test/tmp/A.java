package test.tmp;

import org.testng.annotations.Test;

public class A extends Base {
   @Test(groups = "foo")
   public void a() {
       System.out.println( "a" );
   }
}