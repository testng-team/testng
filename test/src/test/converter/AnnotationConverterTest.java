package test.converter;


import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.Type;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.testng.AnnotationConverter;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AnnotationConverterTest {

  /**
   *
   * @param fileName The filename to parse
   * @param regexp The regular expression
   * @param resultLines An out parameter that will contain all the lines
   * that matched the regexp
   * @return A List<Integer> containing the lines of all the matches
   *
   * Note that the size() of the returned valuewill always be equal to
   * result.size() at the end of this function.
   */
  static List<Integer> grep(File fileName, String regexp, List<String> resultLines) {
    List<Integer> resultLineNumbers = new ArrayList<Integer>();
    BufferedReader fr = null;
    try {
      fr = new BufferedReader(new FileReader(fileName));
      String line = fr.readLine();
      int currentLine = 0;
      Pattern p = Pattern.compile(".*" + regexp + ".*");

      while(null != line) {
//        ppp("COMPARING " + p + " TO @@@" + line + "@@@");
        if(p.matcher(line).matches()) {
          resultLines.add(line);
          resultLineNumbers.add(currentLine);
        }

        line = fr.readLine();
        currentLine++;
      }
    }
    catch(FileNotFoundException e) {
      e.printStackTrace();
    }
    catch(IOException e) {
      e.printStackTrace();
    }
    finally {
      if(null != fr) {
        try {
          fr.close();
        }
        catch(IOException ex) {
          ex.printStackTrace();
        }
      }
    }

    return resultLineNumbers;

  }

  public static void ppp(String s) {
    System.out.println("[AnnotationConverterTest] " + s);
  }

  /**
   * @param fileName
  * @param tag
  * @param expected A list of line numbers where the tag is expected
   * to be present
   */
  private void checkOutput(String packageName, String fileName, String tag, List expected) {
    String outputDir = System.getProperty("java.io.tmpdir");
    String packageDir = packageName.replace('.', File.separatorChar);
    List<String> resultLines = new ArrayList<String>();
    File file = new File(outputDir, packageDir + File.separatorChar + fileName);
    List<Integer> actualLineNumbers = grep(file, tag, resultLines);
    Assert.assertEquals(actualLineNumbers, expected, file + "\n    tag:" + tag);

  }

  /**
   * @param sourceDir
   * @param annotationType
   * @param outputDir
   */
  private void convertSource(File sourceDir, String annotationType) {
    String outputDir = System.getProperty("java.io.tmpdir");
    List<String> args = new ArrayList<String>();
    args.add("-quiet");
    args.add(annotationType);
    args.add("-srcdir");
    args.add(sourceDir.getAbsolutePath());
    args.add("-d");
    args.add(outputDir);
    String[] argv = args.toArray(new String[args.size()]);
    AnnotationConverter.main(argv);
  }

  private void runAnnotationTest(File sourcePath, String pkg, String fileName, List<?>[] expected) {
    convertSource(sourcePath, "-annotation");
    checkOutput(pkg, fileName, "@Test", expected[0]);
    checkOutput(pkg, fileName, "@BeforeSuite", expected[1]);
    checkOutput(pkg, fileName, "@AfterMethod", expected[2]);
    checkOutput(pkg, fileName, "@ExpectedExceptions", expected[3]);
    checkOutput(pkg, fileName, "@DataProvider", expected[4]);
    checkOutput(pkg, fileName, "@Factory", expected[5]);
    checkOutput(pkg, fileName, "@Parameters", expected[6]);
  }

  @Test(parameters = { "source-directory" })
  public void testAnnotations(String dir) {
    runAnnotationTest(new File(dir), "test.sample", "ConverterSample3.java",
      new List[] {
        Arrays.asList(13, 27, 38, 46, 71, 95, 104), Arrays.asList(19), Arrays.asList(54),
        Arrays.asList(37, 70), Arrays.asList(79), Arrays.asList(87), Arrays.asList(69)
      });
  }

  @Test(parameters = { "source-directory" })
  public void testAnnotationsNoPackage(String dir) {
    runAnnotationTest(new File(dir, "../../.."), "", "ConverterSample4.java",
      new List[] {
        Arrays.asList(28, 39, 47, 71), Arrays.asList(20), Arrays.asList(55), Arrays.asList(38, 70),
        Arrays.asList(79), Arrays.asList(87), Arrays.asList()
      });
  }

  @Test(parameters = { "source-directory" })
  public void testQdoxExtractsLineNumbers(String dir) {
    JavaDocBuilder builder = new JavaDocBuilder();
    try {
      builder.addSource(new File(dir, "ConverterSample3.java"));
    }
    catch(Exception e) {
      Assert.fail("Could not read specified file");
    }
    JavaClass fooClass = builder.getClassByName("test.sample.ConverterSample3");
    Assert.assertEquals(fooClass.getLineNumber(), 13,
      "An older (or regressed) version of qdox.jar will report 0 as the line number");
    JavaMethod getIMethod = fooClass.getMethodBySignature("plainTest", Type.EMPTY_ARRAY);
    Assert.assertEquals(getIMethod.getLineNumber(), 25,
      "An older (or regressed) version of qdox.jar will report 0 as the line number");
  }

}
