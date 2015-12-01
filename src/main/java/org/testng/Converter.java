package org.testng;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import org.testng.collections.Sets;
import org.testng.internal.Yaml;
import org.testng.xml.Parser;
import org.testng.xml.XmlSuite;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Convert XML files to YAML and vice versa.
 * 
 * @author cbeust
 */
public class Converter {

  @Parameter(description = "file1 [file2 file3...]", required = true)
  private List<String> m_files;

  @Parameter(names = "-d", description = "The directory where the file(s) will be created")
  private String m_outputDirectory = ".";

  public static void main(String[] args)
      throws ParserConfigurationException, SAXException, IOException {
    Converter c = new Converter();
    c.run(args);
  }

  private void findAllSuites(Collection<XmlSuite> suites, Set<XmlSuite> result) {
    for (XmlSuite s : suites) {
      result.add(s);
      for (XmlSuite xs : s.getChildSuites()) {
        findAllSuites(Arrays.asList(xs), result);
      }
    }
  }

  private void run(String[] args)
      throws ParserConfigurationException, SAXException, IOException {
    JCommander jc = new JCommander(this);
    try {
      jc.parse(args);
      File f = new File(m_outputDirectory);
      if (! f.exists()) f.mkdir();

      for (String file : m_files) {
        Set<XmlSuite> allSuites = Sets.newHashSet();
        Parser parser = new Parser(file);
        parser.setLoadClasses(false);  // we might not have these classes on the classpath
        findAllSuites(parser.parse(), allSuites);

        for (XmlSuite suite : allSuites) {
          String fileName = suite.getFileName();
          int ind = fileName.lastIndexOf(".");
          String bn = fileName.substring(0, ind);
          int ind2 = bn.lastIndexOf(File.separatorChar);
          String baseName = bn.substring(ind2 + 1);

          if (file.endsWith(".xml")) {
            File newFile = new File(m_outputDirectory, baseName + ".yaml");
            writeFile(newFile, Yaml.toYaml(suite).toString());
          }
          else if (file.endsWith(".yaml")) {
            File newFile = new File(m_outputDirectory, baseName + ".xml");
            writeFile(newFile, suite.toXml());
          }
          else {
            throw new TestNGException("Unknown file type:" + file);
          }
        }
      }
    }
    catch(ParameterException ex) {
      System.out.println("Error: " + ex.getMessage());
      jc.usage();
    }
  }

  private void writeFile(File newFile, String content) throws IOException {
    try (FileWriter bw = new FileWriter(newFile)) {
      bw.write(content);
    }
    System.out.println("Wrote " + newFile);
  }
}
