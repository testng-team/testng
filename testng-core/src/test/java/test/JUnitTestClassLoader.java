package test;

import static org.testng.Assert.*;

import java.io.File;
import java.util.List;
import org.testng.*;
import org.testng.annotations.Test;
import testhelper.CompiledCode;
import testhelper.SimpleCompiler;
import testhelper.SourceCode;

public class JUnitTestClassLoader extends ClassLoader {

  @Test
  public void testPassAndFail() throws Exception {
    String src =
        "import org.junit.Test;\n"
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
    String src =
        "import org.junit.Test;\n"
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
    String src =
        "import org.junit.Test;\n"
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
    assertNotEquals(
        testClass.getClassLoader(),
        this.getClass().getClassLoader(),
        "JUnit test must be loaded by a different classloader");
    try {
      this.getClass().getClassLoader().loadClass(testClass.getName());
      fail("it must be imposiible to load JUnit test by current classloader");
    } catch (ClassNotFoundException c) {
    }

    tng.setTestClasses(new Class<?>[] {testClass});
    tng.addListener(listener);
    tng.run();
    return listener;
  }

  private Class<?> compile(String src, String name) throws Exception {
    File directory = SimpleCompiler.createTempDir();
    SourceCode sourceCode = new SourceCode(name, src, directory, false);
    List<CompiledCode> compiledCode = SimpleCompiler.compileSourceCode(sourceCode);
    byte[] bytes = compiledCode.get(0).getByteCode();
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

    public void onTestStart(ITestResult result) {}

    public void onTestSkipped(ITestResult result) {}

    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {}

    public void onStart(ITestContext context) {}

    public void onFinish(ITestContext context) {}
  }
}
