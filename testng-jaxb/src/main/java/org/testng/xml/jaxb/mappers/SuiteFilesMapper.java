package org.testng.xml.jaxb.mappers;

import java.util.List;
import java.util.stream.Collectors;
import org.testng.xml.jaxb.SuiteFile;
import org.testng.xml.jaxb.SuiteFiles;

public class SuiteFilesMapper {

  public static List<String> toXmlSuiteFiles(SuiteFiles suiteFiles) {
    return suiteFiles.getSuiteFile().stream().map(SuiteFile::getPath).collect(Collectors.toList());
  }
}
