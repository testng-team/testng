package test.preserveorder;

import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import test.BaseLogTest;
import test.SimpleBaseTest;

public class PreserveOrderTest extends SimpleBaseTest {

  @Test
  public void preserveClassOrder() {
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
      xt.setPreserveOrder("true");
//      System.out.println(s.toXml());
      tng.setXmlSuites(Arrays.asList(s));
      tng.run();

      // 3 methods per class, 3 classes, so the log should contain 9 calls
      List<String> log = BaseLogTest.getLog();
      Assert.assertEquals(log.size(), 9);

      // Every slice of 3 logs should belong to the same class in the same
      // order as in the specified string:  AAA.a1, AAA.a2, AAA.a3, B.a1, etc...
      // Since we're only testing class ordering in this test, we only match
      // against the class name
      for (int i = 0; i < t.length; i++) {
        for (int j = 0; j < 3; j++) {
          Assert.assertTrue(log.get(j + (3 * i)).startsWith(t[i] + "."));
        }
      }
    }
  }

  @Test
  public void preserveMethodOrder() {
    String[][] methods = new String[][] {
        new String[] { "a1", "a2", "a3" },
        new String[] { "a1", "a3", "a2" },
        new String[] { "a2", "a1", "a3" },
        new String[] { "a2", "a3", "a1" },
        new String[] { "a3", "a2", "a1" },
        new String[] { "a3", "a1", "a2" },
    };

    for (String[] m : methods) {
      TestNG tng = create();
  //      tng.setVerbose(2);
      XmlSuite s = createXmlSuite("Suite");
      String testName = "test.preserveorder.AAA";
      XmlTest xt = createXmlTest(s, "Test", testName);
      addMethods(xt.getXmlClasses().get(0), m);
      xt.setPreserveOrder("true");
  //      System.out.println(s.toXml());
      tng.setXmlSuites(Arrays.asList(s));
      tng.run();

      List<String> log = BaseLogTest.getLog();
//      System.out.println(log);
      for (int i = 0; i < log.size(); i++) {
        if (!log.get(i).endsWith(m[i])) {
          throw new AssertionError("Expected " + Arrays.asList(m) + " but got " + log);
        }
      }
    }

  }
}
