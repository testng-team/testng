package test.jar;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

import java.io.File;

public class JarTest {
  private TestListenerAdapter init(String jarFile) {
    TestNG tng = new TestNG();
    File currentDir = new File(".");
    String path = currentDir.getAbsolutePath();
    char s = File.separatorChar;
    path = path + s + "test" + s + "src" + s + "test" + s + "jar" + s;
    String finalPath = path + jarFile;
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
    Assert.assertEquals(tla.getPassedTests().get(0).getName(), "f");
  }

  @Test
  public void jarWithoutTestngXml() {
    TestListenerAdapter tla = init("withouttestngxml.jar");
    Assert.assertEquals(tla.getPassedTests().size(), 2);
    String first = tla.getPassedTests().get(0).getName();
    String second = tla.getPassedTests().get(1).getName();
    Assert.assertTrue("f".equals(first) || "g".equals(first));
    Assert.assertTrue("f".equals(second) || "g".equals(second));
  }
}
