package test.converter;

import org.testng.Assert;
import org.testng.JUnitConverter;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class JUnitConverterTest {
  
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
  static public List grep(File fileName, String regexp, List resultLines) {
    List resultLineNumbers = new ArrayList();
    try {
      BufferedReader fr = new BufferedReader(new FileReader(fileName));
      String line = fr.readLine();
      int currentLine = 0;
      Pattern p = Pattern.compile(".*" + regexp + ".*");
      
      while (null != line) {
//        ppp("COMPARING " + p + " TO @@@" + line + "@@@");
         if (p.matcher(line).matches()) {
           resultLines.add(line);
           resultLineNumbers.add(currentLine);
         }
         
         line = fr.readLine();
         currentLine++;
      }
    }
    catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    
    return resultLineNumbers;
    
  }
  
  public static void ppp(String s) {
    System.out.println("[JUnitConverterTest] " + s);
  }
  
  /**
   * @param fileName
   * @param tag
   * @param annotationType
   * @param expected A list of line numbers where the tag is expected
   * to be present
   */
  private void runTest(File sourceDir, String packageName, String fileName, String tag, 
      String annotationType, List expected) 
  {
    String outputDir = System.getProperty("java.io.tmpdir");
    String packageDir = packageName.replace('.', File.separatorChar);
    List args = new ArrayList();
    args.add("-quiet");
    args.add(annotationType);
    args.add("-srcdir");
    args.add(sourceDir.getAbsolutePath());
    args.add("-d");
    args.add(outputDir);
    String[] argv = (String[]) args.toArray(new String[args.size()]);
    JUnitConverter.main(argv);
    
    List resultLines = new ArrayList();
    File file = new File(outputDir, packageDir + File.separatorChar + fileName);
    List actualLineNumbers = grep(file, tag, resultLines);
    Assert.assertEquals(actualLineNumbers, expected, file + "\n    tag:" + tag);
    
  }
  
  private void runJavaDocTest(File sourcePath, String pkg, String fileName, List[] expected) {
    runTest(sourcePath, pkg, fileName, "@testng.test", "-javadoc", expected[0]);
    runTest(sourcePath, pkg, fileName, "@testng.before-method", "-javadoc", expected[1]);
    runTest(sourcePath, pkg, fileName, "@testng.after-method", "-javadoc", expected[2]);
  }

  private void runAnnotationTest(File sourcePath, String pkg, String fileName, List[] expected) {
    runTest(sourcePath, pkg, fileName, "@Test", "-annotation", expected[0]);
    runTest(sourcePath, pkg, fileName, "@BeforeMethod", "-annotation", expected[1]);
    runTest(sourcePath, pkg, fileName, "@AfterMethod", "-annotation", expected[2]);
  }

  @Test(parameters = { "source-directory" })
  public void testAnnotations(String dir) {
    runAnnotationTest(new File(dir),  "test.converter", "ConverterSample1.java", 
        new List[] { Arrays.asList(23, 30, 35), Arrays.asList(9), Arrays.asList(18) });
  }
  
  @Test(parameters = { "source-directory" })
  public void testAnnotationsNoPackage(String dir) {
    runAnnotationTest(new File(dir, "../../.."),  "", "ConverterSample2.java", 
        new List[] { Arrays.asList(23, 30, 35), Arrays.asList(9), Arrays.asList(18) });
  }
  
  @Test(parameters = { "source-directory" })
  public void testJavaDoc(String dir) {
    runJavaDocTest(new File(dir),  "test.converter", "ConverterSample1.java", 
        new List[] { Arrays.asList(25, 34, 41), Arrays.asList(7), Arrays.asList(18) });
  }

  @Test(parameters = { "source-directory" })
  public void testJavaDocNoPackage(String dir) {
    runJavaDocTest(new File(dir, "../../.."),  "", "ConverterSample2.java",
        new List[] { Arrays.asList(25, 34, 41), Arrays.asList(7), Arrays.asList(18) });
  }

  
}
