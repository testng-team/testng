package test.junitreports;

import java.io.IOException;
import java.io.InputStream;
import org.testng.TestNGException;
import org.testng.xml.XMLParser;
import org.xml.sax.SAXException;

public class TestsuiteXmlParser extends XMLParser<Testsuite> {

  @Override
  public Testsuite parse(String filePath, InputStream is, boolean loadClasses)
      throws TestNGException {
    TestSuiteHandler handler = new TestSuiteHandler();
    try {
      super.parse(is, handler);
      return handler.getTestsuite();
    } catch (SAXException | IOException e) {
      throw new TestNGException(e);
    }
  }
}
