package test.factory;

import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import java.util.ArrayList;
import java.util.List;

/**
 * Test that setUp methods are correctly interleaved even
 * when we use similar instances of a same test class.
 * 
 * @author cbeust
 */
public class Sample2 {
  private static List<String> m_methodList = new ArrayList<String>();

  @Configuration(beforeSuite = true)
  public void init() {
    m_methodList = new ArrayList<String>();
  }
  
  @Configuration(beforeTestMethod = true)
  public void setUp() {
    ppp("SET UP");
    m_methodList.add("setUp");
  }
  
  @Configuration(afterTestMethod = true)
  public void tearDown() {
    ppp("TEAR DOWN");
    m_methodList.add("tearDown");
  }

  @Configuration(afterSuite = true)
  public void afterSuite() {
    String[] expectedStrings = {
        "setUp",
        "testInputImages",
        "tearDown",
        "setUp",
        "testInputImages",
        "tearDown",
        "setUp",
        "testImages",
        "tearDown",
        "setUp",
        "testImages",
        "tearDown",
    };
    List<String> expected = new ArrayList<String>();
    for (String s : expectedStrings) {
      expected.add(s);
    }
    
    ppp("ORDER OF METHODS:");
    for (String s : m_methodList) {
      ppp("   " + s);
    }
    
    assertEquals(m_methodList, expected);
  }
  
  @Test
  public void testInputImages() {
    m_methodList.add("testInputImages");
    ppp("TESTINPUTIMAGES");
  }
  
  @Test(dependsOnMethods={"testInputImages"})
  public void testImages() {
    m_methodList.add("testImages");
  }
  
  private static void ppp(String s) {
    if (false) {
      System.out.println("[Sample2] " + s);
    }
  }
}
