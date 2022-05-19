package org.testng.testhelper;

import java.io.File;
import java.io.IOException;
import java.util.List;

public final class TestClassGenerator {
  private static final File projectDir = SimpleCompiler.createTempDir();

  private TestClassGenerator() {
    // Utility class. Defeat instantiation.
  }

  public static File getProjectDir() {
    return projectDir;
  }

  public static SourceCode[] generate(String packageName, List<String> classNames) {
    return classNames.stream()
        .map(className -> generateCode(packageName, className))
        .toArray(SourceCode[]::new);
  }

  private static SourceCode generateCode(String packageName, String className) {
    String source = "package " + packageName + ";\n\n";
    source += "import org.testng.annotations.Test;\n";
    source += "public class " + className + " {\n";
    source += "  @Test\n";
    source += "  public void testMethod() {\n";
    source += "  }\n";
    source += "}\n";
    try {
      return new SourceCode(packageName, className, source, projectDir, false);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
