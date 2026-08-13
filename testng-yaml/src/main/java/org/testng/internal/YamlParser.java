package org.testng.internal;

import java.io.FileNotFoundException;
import java.io.InputStream;
import org.testng.TestNGException;
import org.testng.xml.ISuiteParser;
import org.testng.xml.XmlSuite;
import org.testng.xml.internal.Parser;
import org.yaml.snakeyaml.error.YAMLException;

public class YamlParser implements ISuiteParser {

  @Override
  public XmlSuite parse(String filePath, InputStream is, boolean loadClasses)
      throws TestNGException {
    try {
      return Yaml.parse(filePath, is, loadClasses);
    } catch (FileNotFoundException | YAMLException e) {
      // YAMLException covers a malformed document and a key outside the schema. Wrapped so that a
      // bad YAML suite fails the way a bad XML one does -- SuiteXmlParser wraps its SAX failures
      // the same way, and ISuiteParser declares TestNGException.
      throw new TestNGException(e);
    }
  }

  @Override
  public boolean accept(String fileName) {
    return Parser.hasFileScheme(fileName)
        && (fileName.endsWith(".yaml") || fileName.endsWith(".yml"));
  }
}
