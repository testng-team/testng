package test.configuration;

import org.testng.annotations.Test;

public class BaseGroupsASampleTest extends Base {
   @Test(groups = "foo")
   public void a() {
//       System.out.println( "a" );
   }
}