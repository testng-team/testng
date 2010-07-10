package test.configuration;

import org.testng.annotations.Test;

public class BaseGroupsBSampleTest extends Base {
   @Test(groups = "foo")
   public void b() {
//       System.out.println( "b" );
   }
}