package test.failures;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.regex.Pattern;
import org.testng.Assert;
import org.testng.TestNG;
import org.testng.reporters.FailedReporter;
import test.SimpleBaseTest;

public abstract class BaseFailuresTest extends SimpleBaseTest {

  protected static TestNG run(TestNG result, Class<?>[] classes, String outputDir) {
    result.setVerbose(0);
    result.setOutputDirectory(outputDir);
    result.setTestClasses(classes);
    result.run();

    return result;
  }

  protected static boolean containsRegularExpressions(Path f, String[] strRegexps) {
    Pattern[] matchers = new Pattern[strRegexps.length];
    boolean[] results = new boolean[strRegexps.length];
    for (int i = 0; i < strRegexps.length; i++) {
      matchers[i] = Pattern.compile(".*" + strRegexps[i] + ".*");
      results[i] = false;
    }

    try (BufferedReader br = Files.newBufferedReader(f, Charset.forName("UTF-8"))) {
      String line = br.readLine();
      while (line != null) {
        for (int i = 0; i < strRegexps.length; i++) {
          if (matchers[i].matcher(line).matches()) {
            results[i] = true;
          }
        }
        line = br.readLine();
      }
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }

    for (int i = 0; i < results.length; i++) {
      if (!results[i]) {
        throw new AssertionError("Couldn't find " + strRegexps[i]);
      }
    }

    return true;
  }

  protected static void verify(Path outputDir, String suiteName, String[] expected)
      throws IOException {
    Path f = outputDir.resolve(suiteName).resolve(FailedReporter.TESTNG_FAILED_XML);
    Assert.assertTrue(containsRegularExpressions(f, expected));

    Files.walkFileTree(
        outputDir,
        new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
              throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
          }
        });
    Files.deleteIfExists(outputDir);
  }
}
