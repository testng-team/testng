package test.jar;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

import java.io.File;
import java.util.Arrays;

public class JarTest extends SimpleBaseTest {
  private TestListenerAdapter init(String jarFile) {
    TestNG tng = create();
    String finalPath = getPathToResource(jarFile);
    tng.setTestJar(finalPath);
    TestListenerAdapter result = new TestListenerAdapter();
    tng.addListener(result);
    tng.run();

    return result;
  }

  @Test
  public void jarWithTestngXml() {
    TestListenerAdapter tla = init("withtestngxml.jar");
    Assert.assertEquals(tla.getPassedTests().size(), 1);
    Assert.assertEquals(tla.getPassedTests().get(0).getMethod().getMethodName(), "f");
  }

  @Test
  public void jarWithoutTestngXml() {
    TestListenerAdapter tla = init("withouttestngxml.jar");
    Assert.assertEquals(tla.getPassedTests().size(), 2);
    String first = tla.getPassedTests().get(0).getMethod().getMethodName();
    String second = tla.getPassedTests().get(1).getMethod().getMethodName();
    Assert.assertTrue("f".equals(first) || "g".equals(first));
    Assert.assertTrue("f".equals(second) || "g".equals(second));
  }

  @Test
  public void jarWithTestngXmlOverriddenOnCommandLine() {
    TestNG tng = create();
    String finalPath = getPathToResource("withtestngxml.jar");
    tng.setTestJar(finalPath);
    tng.setTestSuites(Arrays.asList(getPathToResource("testng-override.xml")));
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    tng.run();

    Assert.assertEquals(tla.getPassedTests().size(), 2);
    String first = tla.getPassedTests().get(0).getMethod().getMethodName();
    String second = tla.getPassedTests().get(1).getMethod().getMethodName();
    Assert.assertTrue("f".equals(first) || "g".equals(first));
    Assert.assertTrue("f".equals(second) || "g".equals(second));

  }
}
