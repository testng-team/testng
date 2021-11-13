package testhelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import org.testng.TestNGException;
import org.testng.collections.Lists;

public class SimpleCompiler {

  public static List<CompiledCode> compileSourceCode(SourceCode... sources) throws IOException {
    JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
    String[] arguments = new String[sources.length];
    int i = 0;
    for (SourceCode source : sources) {
      arguments[i++] = source.getLocation().getCanonicalPath();
    }
    int rc = javac.run(null, null, null, arguments);
    if (rc != 0) {
      throw new TestNGException("Encountered errors when compiling code");
    }
    List<CompiledCode> compiledCodes = Lists.newArrayList();
    for (SourceCode source : sources) {
      source.getLocation().delete();
      CompiledCode compiledCode =
          new CompiledCode(source.getName(), source.getDirectory(), source.isSkipLoading());
      compiledCodes.add(compiledCode);
    }
    return compiledCodes;
  }

  public static File createTempDir() {
    try {
      return Files.createTempDirectory("custom").toFile();
    } catch (IOException e) {
      throw new TestNGException(e);
    }
  }
}
