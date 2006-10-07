package org.testng.internal;


import java.io.File;

import java.util.HashMap;
import java.util.Map;

/**
 * Scans the directory and its subdirectories for java files to convert to TestNG format
 * @author micheb10 12-Sep-2006
 * @since 5.3
 */
public class AnnotationDirectoryConverter {

  private File m_sourceDir;
  private File m_outDir;
  private Map<File, File> m_fileNames;

  /**
   * @param sourceDirectory
   * @param destinationDirectory
   */
  public AnnotationDirectoryConverter(File sourceDirectory, File destinationDirectory) {
    m_sourceDir= sourceDirectory;
    m_outDir= destinationDirectory;
  }

  /**
   * @return
   */
  public int convert() {

    //
    // Convert annotations
    //
    m_fileNames= convert(m_sourceDir);
    File[] files= m_fileNames.keySet().toArray(new File[m_fileNames.size()]);

    AnnotationTestConverter fc= new AnnotationTestConverter(files, m_outDir);

    int converted= fc.convert();

    return converted;
  }

  private boolean isTestFile(File f) {
    return f.getName().endsWith(".java");
  }

  private Map<File, File> convert(File f) {
    Map<File, File> result= new HashMap<File, File>();
    if(f.isDirectory()) {
      File[] files= f.listFiles();
      for(File file : files) {
        File f2= file.getAbsoluteFile();
        Map<File, File> others= convert(f2);
        result.putAll(others);
      }
    }
    else {
      if(isTestFile(f)) {
        result.put(f, f);
      }
    }

    return result;
  }

}
