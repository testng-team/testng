package test.jarpackages;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

import java.io.File;

public class JarPackagesTest extends SimpleBaseTest {
  private TestListenerAdapter init(String jarFile) {
    TestNG tng = create();
    File currentDir = new File(".");
    String path = currentDir.getAbsolutePath();
    char s = File.separatorChar;
    path = path + s + "test" + s + "src" + s + "test" + s + "jarpackages" + s;
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
    Assert.assertEquals(tla.getPassedTests().size(), 2);
    String first = tla.getPassedTests().get(0).getName();
    String second = tla.getPassedTests().get(1).getName();
    boolean fThenG = "f".equals(first) && "g".equals(second);
    boolean gThenF = "g".equals(first) && "f".equals(second);
    Assert.assertTrue(fThenG || gThenF);
  }

  @Test
  public void jarWithoutTestngXml() {
    TestListenerAdapter tla = init("withouttestngxml.jar");
    Assert.assertEquals(tla.getPassedTests().size(), 2);
    String first = tla.getPassedTests().get(0).getName();
    String second = tla.getPassedTests().get(1).getName();
    boolean fThenG = "f".equals(first) && "g".equals(second);
    boolean gThenF = "g".equals(first) && "f".equals(second);
    Assert.assertTrue(fThenG || gThenF);
  }
}
