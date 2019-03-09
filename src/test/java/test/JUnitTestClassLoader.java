package test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.testng.*;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class JUnitTestClassLoader extends ClassLoader {

  private static Path createTempDir() {
    try {
      return Files.createTempDirectory("junitclassloader");
    } catch (IOException e) {
      throw new TestNGException(e);
    }

  }

  @Test
  public void testPassAndFail() throws Exception {
    String src = "import org.junit.Test;\n"
               + "import static org.junit.Assert.*;\n"
               + "public class SimpleTest1 {\n"
               + "    @Test\n"
               + "    public void test1() {\n"
               + "        assertEquals(42, 42);\n"
               + "    }\n"
               + "    @Test\n"
               + "    public void test2() {\n"
               + "        assertEquals(42, 0);\n"
               + "    }\n"
               + "}\n";
    Listener listener = runJunitTest(src, "SimpleTest1");
    assertEquals(1, listener.success);
    assertEquals(1, listener.failure);
  }

  @Test
  public void testFail() throws Exception {
    String src = "import org.junit.Test;\n"
               + "import static org.junit.Assert.*;\n"
               + "public class SimpleTest2 {\n"
               + "    @Test\n"
               + "    public void test() {\n"
               + "        assertEquals(42, 0);\n"
               + "    }\n"
               + "}\n";
    Listener listener = runJunitTest(src, "SimpleTest2");
    assertEquals(0, listener.success);
    assertEquals(1, listener.failure);
  }

  @Test
  public void testPass() throws Exception {
    String src = "import org.junit.Test;\n"
               + "import static org.junit.Assert.*;\n"
               + "public class SimpleTest3 {\n"
               + "    @Test\n"
               + "    public void test() {\n"
               + "        assertEquals(42, 42);\n"
               + "    }\n"
               + "}\n";
    Listener listener = runJunitTest(src, "SimpleTest3");
    assertEquals(1, listener.success);
    assertEquals(0, listener.failure);
  }

  private Listener runJunitTest(String src, String name) throws Exception {
    TestNG tng = new TestNG(false);
    Class<?> testClass = compile(src, name);
    Listener listener = new Listener();
    tng.setJUnit(true);
    tng.addClassLoader(testClass.getClassLoader());
    assertNotEquals(testClass.getClassLoader(), this.getClass().getClassLoader(),
                    "JUnit test must be loaded by a different classloader");
    try {
        this.getClass().getClassLoader().loadClass(testClass.getName());
        fail("it must be imposiible to load JUnit test by current classloader");
    } catch (ClassNotFoundException c) {
    }

    tng.setTestClasses(new Class<?>[]{testClass});
    tng.addListener(listener);
    tng.run();
    return listener;
  }

  private Class<?> compile(String src, String name) throws Exception {
    // compile class and load it into by a custom classloader
    File directory = createTempDir().toFile();
    File srcFile = new File(directory, name + ".java");
    Files.write(srcFile.toPath(), src.getBytes(), StandardOpenOption.CREATE_NEW);
    JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
    assertEquals(0, javac.run(null, null, null, srcFile.getCanonicalPath()));
    srcFile.delete();
    File classFile = new File(directory, name + ".class");

    byte[] bytes = Files.readAllBytes(classFile.toPath());
    classFile.delete();
    return defineClass(name, bytes, 0, bytes.length);
  }

  private static class Listener implements ITestListener {
    public int success = 0;
    public int failure = 0;

    public void onTestSuccess(ITestResult result) {
      success++;
    }

    public void onTestFailure(ITestResult result) {
      failure++;
    }

    public void onTestStart(ITestResult result) { }
    public void onTestSkipped(ITestResult result) { }
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) { }
    public void onStart(ITestContext context) { }
    public void onFinish(ITestContext context) { }
  }

}
