package test.preserveorder;

import java.util.Arrays;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import test.BaseLogTest;
import test.SimpleBaseTest;

public class PreserveOrderTest extends SimpleBaseTest {

  @Test
  public void withPreserveOrder() {
    String[][] tests = new String[][] {
      new String[] { "AAA", "B", "C" },
      new String[] { "AAA", "C", "B" },
      new String[] { "B", "AAA", "C" },
      new String[] { "B", "C", "AAA" },
      new String[] { "C", "B", "AAA" },
      new String[] { "C", "AAA", "B" },
    };
    for (String[] t : tests) {
      TestNG tng = create();
//      tng.setVerbose(2);
      XmlSuite s = createXmlSuite("Suite");
      String[] fullTestNames = new String[t.length];
      for (int i = 0; i < t.length; i++) {
        fullTestNames[i] = "test.preserveorder." + t[i];
      }
      XmlTest xt = createXmlTest(s, "Test", fullTestNames);
      xt.setPreserveOrder(true);
//      System.out.println(s.toXml());
      tng.setXmlSuites(Arrays.asList(s));
      tng.run();
      Assert.assertEquals(BaseLogTest.getLog(), Arrays.asList("A", "B", "C"));
    }
  }
}
