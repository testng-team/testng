package org.testng.xml;

import org.testng.TestNGException;
import org.testng.remote.strprotocol.IRemoteSuiteListener;
import org.testng.remote.strprotocol.IRemoteTestListener;
import org.testng.reporters.XMLReporterListener;
import org.xml.sax.SAXException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ResultXMLParser extends XMLParser<Object> {
  private IRemoteTestListener m_testListener;
  private IRemoteSuiteListener m_suiteListener;

  public ResultXMLParser(IRemoteSuiteListener suiteListener, IRemoteTestListener testListener) {
    m_suiteListener = suiteListener;
    m_testListener = testListener;
  }

  public void parse() {
  }

  @Override
  public Object parse(String currentFile, InputStream inputStream) {
    ResultContentHandler handler = new ResultContentHandler(m_suiteListener, m_testListener);

    try {
      m_saxParser.parse(inputStream, handler);

      return null;
    }
    catch (FileNotFoundException e) {
      throw new TestNGException(e);
    } catch (SAXException e) {
      throw new TestNGException(e);
    } catch (IOException e) {
      throw new TestNGException(e);
    }
  }

  public static void main(String[] args) throws FileNotFoundException {
//    ResultXmlParser parser = new ResultXmlParser();
//    String fileName = "/Users/cbeust/java/testng/test-output/testng-results.xml";
//    parser.parse(fileName, new FileInputStream(new File(fileName)));
  }
}
