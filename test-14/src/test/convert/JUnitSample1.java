package test.convert;

import junit.framework.*;

/**
 * This class
 *
 * @author Cedric Beust, May 5, 2004
 */
public class JUnitSample1 extends TestCase {
   private String m_field = null;

   public JUnitSample1() {
      super();
   }

   public JUnitSample1(String n) {
      super(n);
   }

   public void setUp() {
      m_field = "foo";
   }

   public void tearDown() {
      m_field = null;
   }

   /**
    * Testing method Sample1_1.
    * 
    */
   public void testSample1_1() {
   }

   public void testSample1_2() {

   }

   private static void ppp(String s) {
      System.out.println("[JUnitSample1] " + s);
   }


}
