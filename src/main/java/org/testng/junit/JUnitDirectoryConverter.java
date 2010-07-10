/*
 * $Id$
 * $Date$
 */
package org.testng.junit;


import org.testng.collections.Maps;
import org.testng.internal.Utils;
import org.testng.reporters.XMLStringBuffer;
import org.testng.xml.Parser;

import java.io.File;
import java.util.Map;
import java.util.Properties;

/**
 * Tool class scanning and converting the JUnit sources found in a directory.
 *
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 * @author <a href="mailto:the_mindstorm@evolva.ro">the_mindstorm</a>
 */
public class JUnitDirectoryConverter {
   private File m_sourceDir = null;
   private File m_outDir    = null;
   private String m_release   = null;
   private boolean m_useAnnotations;
   private String[] m_groups = null;

   private Map<File, File> m_fileNames = Maps.newHashMap();

   /**
    * Sole constructor.
    *
    * @param srcDir path to directory containing JUnit tests
    * @param outDir path to output directory for the generated test sources
    * @param useAnnotation flag for JDK 1.5 annotation use. <tt>true</tt> if annotations should be
    *                      used, <tt>false</tt> if javadoc-like annotations should be use
    * @param restore flag if the output directory should reflect the package
    */
   public JUnitDirectoryConverter(File srcDir, File outDir, String release, 
       boolean useAnnotation, String[] groups) 
   {
      m_sourceDir = srcDir;
      m_outDir    = outDir;
      m_useAnnotations = useAnnotation;
      m_release = release;
      m_groups = groups;
   }

   public int convert() {
      //
      // Insert annotations
      //
      m_fileNames = convert(m_sourceDir);
      File[] files = m_fileNames.keySet().toArray(new File[m_fileNames.size()]);

      JUnitTestConverter fc = new JUnitTestConverter(files,
                                                     m_outDir,
                                                     m_release,
                                                     m_useAnnotations,
                                                     m_groups);

      int converted = fc.convert();

      if(-1 == converted) {
         return converted;
      }

      generateConfiguration(fc.getClassNames());

      return converted;
   }

   private void generateConfiguration(String[] classNames) {
      //
      // Create testng.xml
      //
      XMLStringBuffer xsb = new XMLStringBuffer("");
      xsb.setDocType("suite SYSTEM \"" + Parser.TESTNG_DTD_URL + "\"");
      Properties props = new Properties();
      props.setProperty("name", "Generated Suite");
      xsb.push("suite", props);
      props.setProperty("name", "Generated Test");
      xsb.push("test", props);
      xsb.push("classes");

      for(String className : classNames) {
         Properties p = new Properties();
         p.setProperty("name", className);
         xsb.addEmptyElement("class", p);
      }

      xsb.pop("classes");
      xsb.pop("test");
      xsb.pop("suite");

      Utils.writeFile(m_outDir.getAbsolutePath(), "testng.xml", xsb.toXML());
   }

   private boolean isTestFile(File f) {
      return f.getName().endsWith(".java");
   }

   private Map<File, File> convert(File f) {
      Map<File, File> result = Maps.newHashMap();
      if(f.isDirectory()) {
         File[] files = f.listFiles();
         for(File file : files) {
            File f2 = file.getAbsoluteFile();
            Map<File, File> others = convert(f2);
            result.putAll(others);
         }
      } else {
         if(isTestFile(f)) {
            result.put(f, f);
         }
      }

      return result;
   }

   private void ppp(String s) {
      System.out.println("[JUnitDirectoryConverter]" + s);
   }
}
