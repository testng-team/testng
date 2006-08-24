package test.convert;

import org.testng.junit.JUnitDirectoryConverter;

import java.io.File;

/**
 * Test unit for JUnitDirectoryConverter
 *
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 */
public class JUnitConverterTest {
   private File m_output;

   /**
    * @testng.test parameters="inputDir"
    */
   public void convert(String inputDir) {
      System.out.println("convert");
      m_output = new File(inputDir, "generated");
      m_output.mkdirs();

      JUnitDirectoryConverter convertor = new JUnitDirectoryConverter(new File(inputDir),
            m_output,
            "1.4",
            false, null);

      int result = convertor.convert();
      assert 2 == result : "Expected number of tests 2, found " + result;
   }

   /**
    * @testng.configuration afterTestMethod=true
    */
   public void clean() {
      File[] files = m_output.listFiles();

      for(int i = 0; i < files.length; i++) {
         files[i].delete();
      }

      m_output.delete();
   }
}
