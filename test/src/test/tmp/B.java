package test.tmp;

import org.testng.annotations.Test;

public class B extends Base {
   @Test(groups = "foo")
   public void b() {
       System.out.println( "b" );
   }
}