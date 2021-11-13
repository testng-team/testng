package test.suites.github1850;

import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.List;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.internal.Parser;
import test.SimpleBaseTest;

/**
 * This test checks that TestNG can handle duplicate child suites when we have the following set of
 * files:
 *
 * <p>- parent-suite-with-duplicate-child -> [child-suite-1, children/child-suite-3, child-suite1,
 * children/child-suite-3] - children/child-suite-3 -> [../child-suite-2, child-suite-4,
 * morechildren/child-suite-5]
 *
 * <p>SHOULD return a XmlSuite object with following structure:
 *
 * <p>parent-suite-with-duplicate-child ├───child-suite-1 ├───child-suite-3 │ ├───child-suite-2 │
 * ├───child-suite-4 │ └───child-suite-5 ├───child-suite-1(0) └───child-suite-3(0)
 * ├───child-suite-2(0) ├───child-suite-4(0) └───child-suite-5(0)
 *
 * <p>but NOT like:
 *
 * <p>parent-suite-with-duplicate-child ├───child-suite-1 ├───child-suite-3 ├───child-suite-1(0)
 * └───child-suite-3(0) ├───child-suite-2 ├───child-suite-4 ├───child-suite-5 ├───child-suite-2(0)
 * ├───child-suite-4(0) └───child-suite-5(0)
 *
 * <p>Check the <code>checksuitesinitialization</code> folder under test resources
 */
public class DuplicateChildSuitesInitializationTest extends SimpleBaseTest {
  @Test
  public void checkDuplicateChildSuites() throws IOException {
    String path =
        getPathToResource("checksuitesinitialization/parent-suite-with-duplicate-child.xml");
    Parser parser = new Parser(path);
    List<XmlSuite> suites = parser.parseToList();
    XmlSuite rootSuite = suites.get(0);
    assertEquals(rootSuite.getChildSuites().size(), 4);

    XmlSuite suite3 = rootSuite.getChildSuites().get(1);
    assertEquals(suite3.getName(), "Child Suite 3");
    assertEquals(suite3.getChildSuites().size(), 3);

    XmlSuite suite3_0 = rootSuite.getChildSuites().get(3);
    assertEquals(suite3.getName(), "Child Suite 3");
    assertEquals(suite3_0.getChildSuites().size(), 3);

    XmlSuite suite5 = suite3.getChildSuites().get(2);
    assertEquals(suite5.getName(), "Child Suite 5");
    assertEquals(suite5.getTests().size(), 1);

    XmlSuite suite5_0 = suite3_0.getChildSuites().get(2);
    assertEquals(suite5_0.getName(), "Child Suite 5");
    assertEquals(suite5_0.getTests().size(), 1);
  }
}
