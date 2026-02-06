package org.testng.xml.jaxb;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import java.io.InputStream;
import org.testng.xml.ISuiteParser;
import org.testng.xml.XmlSuite;
import org.testng.xml.jaxb.mappers.SuiteMapper;

public class JaxbParser implements ISuiteParser {

  @Override
  public XmlSuite parse(String filePath, InputStream is, boolean loadClasses) {
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(Suite.class);
      Suite suite = (Suite) jaxbContext.createUnmarshaller().unmarshal(is);
      return SuiteMapper.toXmlSuite(suite);
    } catch (JAXBException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean accept(String fileName) {
    return fileName.endsWith(".xml");
  }
}
