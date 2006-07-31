package test.sample;

import junit.framework.TestCase;

/**
 * This class
 *
 * @author Cedric Beust, May 5, 2004
 * 
 */
public class JUnitSample1 extends TestCase {
  public static final String EXPECTED2 = "testSample1_2";
  public static final String EXPECTED1 = "testSample1_1";
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

   public void testSample1_1() {
//      System.out.println("testSample1_1");
   }

   public void testSample1_2() {
   }

   private static void ppp(String s) {
      System.out.println("[JUnitSample1] " + s);
   }


}
