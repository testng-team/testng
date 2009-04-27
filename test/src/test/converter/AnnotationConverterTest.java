package test.converter;


import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.Type;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.AnnotationConverter;
import org.testng.Assert;
import org.testng.annotations.Test;

import test.BaseTest;

public class AnnotationConverterTest {

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
    List<Integer> actualLineNumbers = BaseTest.grep(file, tag, resultLines);
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
