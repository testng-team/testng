package test.tmp;

import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

@Test( groups = "foo" )
public class A {
  public A(int n) {}
   @BeforeGroups( "foo" )
   public void beforeGroups() {
       System.out.println( "beforeGroups" );
   }

   @Test()
   public void test() {
       System.out.println( "test" );
   }
   
   public static void main(String[] args) {
//    A a = new A();
  }

}