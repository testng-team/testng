package test.mannotation.issue1976;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.collections.Lists;
import testhelper.CompiledCode;
import testhelper.SimpleCompiler;
import testhelper.SourceCode;

public class IssueTest extends ClassLoader {

  private static final File dir = SimpleCompiler.createTempDir();

  private static final class MyClassLoader extends ClassLoader {

    public Class<?> injectByteCode(CompiledCode byteCode) throws ClassNotFoundException {
      Class<?> clazz =
          defineClass(byteCode.getName(), byteCode.getByteCode(), 0, byteCode.getByteCode().length);
      return loadClass(clazz.getName());
    }
  }

  @Test(
      dataProvider = "dp",
      expectedExceptions = TypeNotPresentException.class,
      description = "GITHUB-1976")
  public void testMethod(SourceCode... sources) throws IOException, ClassNotFoundException {
    TestNG tng = new TestNG(false);
    MyClassLoader classLoader = new MyClassLoader();
    tng.addClassLoader(classLoader);
    List<CompiledCode> byteCodes = SimpleCompiler.compileSourceCode(sources);
    List<Class<?>> classes = Lists.newArrayList();
    for (CompiledCode byteCode : byteCodes) {
      if (byteCode.isSkipLoading()) {
        continue;
      }
      classes.add(classLoader.injectByteCode(byteCode));
    }
    tng.setTestClasses(classes.toArray(new Class[0]));
    tng.run();
    Arrays.stream(sources).forEach(sourceCode -> sourceCode.getLocation().delete());
  }

  @DataProvider(name = "dp")
  public Object[][] getTestData() throws IOException {
    return new Object[][] {
      {new SourceCode[] {missingTypeAtMethodLevel(), exception1()}},
      {new SourceCode[] {missingTypeAtClassLevel(), exception2()}},
      {new SourceCode[] {missingTypeAtConstructor(), dataProvider()}},
      // TODO fix test {new SourceCode[]{missingTypeAtListenerAnnotation(), listener()}},
      {new SourceCode[] {missingTypeAtBaseClass(), childClass(), exception3()}},
    };
  }

  private static SourceCode missingTypeAtMethodLevel() throws IOException {
    String source = "import org.testng.annotations.Test\n;";
    source += "public class Sample1 {\n";
    source += "  @Test(expectedExceptions = MyFancyException.class)\n";
    source += "  public void testMethod() {\n";
    source += "  }\n";
    source += "}\n";
    return new SourceCode("Sample1", source, dir, false);
  }

  private static SourceCode exception1() throws IOException {
    String source = "public class MyFancyException extends RuntimeException {}";
    return new SourceCode("MyFancyException", source, dir, true);
  }

  private static SourceCode missingTypeAtClassLevel() throws IOException {
    String source = "import org.testng.annotations.Test\n;";
    source += "@Test(expectedExceptions = AnotherException.class)\n";
    source += "public class Sample2 {\n";
    source += "  public void testMethod() {\n";
    source += "  }\n";
    source += "}\n";
    return new SourceCode("Sample2", source, dir, false);
  }

  private static SourceCode missingTypeAtBaseClass() throws IOException {
    String source = "@org.testng.annotations.Test(expectedExceptions = ThirdException.class)\n";
    source += "public class MyBaseClass {}\n";
    return new SourceCode("MyBaseClass", source, dir, false);
  }

  private static SourceCode childClass() throws IOException {
    String source = "public class ChildClass extends MyBaseClass {\n;";
    source += "public void testMethod() {}\n";
    source += "}\n";
    return new SourceCode("ChildClass", source, dir, false);
  }

  private static SourceCode exception2() throws IOException {
    String source = "public class AnotherException extends RuntimeException {}";
    return new SourceCode("AnotherException", source, dir, true);
  }

  private static SourceCode exception3() throws IOException {
    String source = "public class ThirdException extends RuntimeException {}";
    return new SourceCode("ThirdException", source, dir, true);
  }

  private static SourceCode missingTypeAtConstructor() throws IOException {
    String source = "import org.testng.annotations.Factory;\n";
    source += "public class FactorySample {\n";
    source += "  @Factory(dataProviderClass = ExampleDP.class)\n";
    source += "  public FactorySample(Object object) { }\n";
    source += "}\n";
    return new SourceCode("FactorySample", source, dir, false);
  }

  private static SourceCode dataProvider() throws IOException {
    String source = "import org.testng.annotations.DataProvider;\n";
    source += "public class ExampleDP {\n";
    source += "  @DataProvider\n";
    source += "  public Object[][] dp() {\n";
    source += "    return new Object[][] {{}};";
    source += "  }\n";
    source += "}";
    return new SourceCode("ExampleDP", source, dir, true);
  }

  private static SourceCode missingTypeAtListenerAnnotation() throws IOException {
    String source = "import org.testng.annotations.Listeners\n";
    source += "@Listeners(SampleListener.class)\n";
    source += "public class Sample4 {}\n";
    return new SourceCode("Sample4", source, dir, false);
  }

  private static SourceCode listener() throws IOException {
    String source = "public class SampleListener implements org.testng.ITestListener {}";
    return new SourceCode("SampleListener", source, dir, true);
  }
}
