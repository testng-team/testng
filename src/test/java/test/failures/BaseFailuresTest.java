package test.failures;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.reporters.FailedReporter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

public class BaseFailuresTest {

//  protected TestNG run(Class[] classes, String outputDir) {
//    return run(new TestNG(), classes, outputDir);
//  }

  protected String getSuiteName() {
    return "TmpSuite";
  }

  protected TestNG run(TestNG result, Class[] classes, String outputDir) {
     result.setVerbose(0);
     result.setOutputDirectory(outputDir);
     result.setTestClasses(classes);
     result.run();

     return result;
  }

  /**
   * @param f
   * @param regexps
   * @return true if the file contains at least one occurrence of each regexp
   */
  protected boolean containsRegularExpressions(File f, String[] strRegexps) {
    Pattern[] matchers = new Pattern[strRegexps.length];
    boolean[] results = new boolean[strRegexps.length];
    for (int i = 0; i < strRegexps.length; i++) {
      matchers[i] = Pattern.compile(".*" + strRegexps[i] + ".*");
      results[i] = false;
    }

    try {
      FileReader fr = new FileReader(f);
      BufferedReader br = new BufferedReader(fr);
      String line = br.readLine();
      while (line != null) {
        for (int i = 0; i < strRegexps.length; i++) {
          if (matchers[i].matcher(line).matches()) {
            results[i] = true;
          }
        }
        line = br.readLine();
      }
      fr.close();
      br.close();
    }
    catch (FileNotFoundException e) {
      e.printStackTrace();
      return false;
    }
    catch (IOException e) {
      e.printStackTrace();
      return false;
    }

    for (int i = 0; i < results.length; i++) {
      boolean result = results[i];
      if (! result) {
        throw new AssertionError("Couldn't find " + strRegexps[i]);
      }
    }

    return true;
  }

  protected void verify(String outputDir, String[] expected) {
    File f = new File(outputDir +
        File.separatorChar + getSuiteName() +
        File.separator + FailedReporter.TESTNG_FAILED_XML);
     boolean passed = containsRegularExpressions(f, expected);
     Assert.assertTrue(passed);

     File tmpDir = new File(outputDir);
     tmpDir.delete();
  }

}
