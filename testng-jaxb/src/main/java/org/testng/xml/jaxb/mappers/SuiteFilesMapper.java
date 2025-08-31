package org.testng.xml.jaxb.mappers;

import java.util.List;
import java.util.stream.Collectors;
import org.testng.xml.XmlSuiteFile;
import org.testng.xml.jaxb.SuiteFile;
import org.testng.xml.jaxb.SuiteFiles;

public class SuiteFilesMapper {

  public static List<XmlSuiteFile> toXmlSuiteFiles(SuiteFiles suiteFiles) {
    return suiteFiles.getSuiteFile().stream()
        .map(
            sf -> {
              XmlSuiteFile xmlSuiteFile = new XmlSuiteFile();
              xmlSuiteFile.setPath(sf.getPath());
              return xmlSuiteFile;
            })
        .collect(Collectors.toList());
  }
}
