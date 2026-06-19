package test.jarpackages;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import org.testng.ITestNGListener;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

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
    tng.addListener((ITestNGListener) result);
    tng.run();

    return result;
  }

  @Test
  public void jarWithTestngXml() {
    TestListenerAdapter tla = init("withtestngxml.jar");
    assertThat(tla.getPassedTests())
        .extracting(result -> result.getName())
        .containsExactlyInAnyOrder("f", "g");
  }

  @Test
  public void jarWithoutTestngXml() {
    TestListenerAdapter tla = init("withouttestngxml.jar");
    assertThat(tla.getPassedTests())
        .extracting(result -> result.getName())
        .containsExactlyInAnyOrder("f", "g");
  }
}
