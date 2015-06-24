package org.testng.xml;

import org.testng.TestNGException;
import org.testng.remote.strprotocol.GenericMessage;
import org.testng.remote.strprotocol.IRemoteSuiteListener;
import org.testng.remote.strprotocol.IRemoteTestListener;
import org.testng.remote.strprotocol.SuiteMessage;
import org.testng.remote.strprotocol.TestMessage;
import org.testng.remote.strprotocol.TestResultMessage;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Parses testng-result.xml.
 *
 * @see ResultContentHandler
 *
 * @author Cedric Beust <cedric@beust.com>
 */
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
  public Object parse(String currentFile, InputStream inputStream, boolean loadClasses) {
    ResultContentHandler handler = new ResultContentHandler(m_suiteListener, m_testListener,
        loadClasses);

    try {
      parse(inputStream, handler);

      return null;
    } catch (SAXException | IOException e) {
      throw new TestNGException(e);
    }
  }

  public static void main(String[] args) throws FileNotFoundException {
    IRemoteSuiteListener l1 = new IRemoteSuiteListener() {

      @Override
      public void onInitialization(GenericMessage genericMessage) {
      }

      @Override
      public void onStart(SuiteMessage suiteMessage) {
      }

      @Override
      public void onFinish(SuiteMessage suiteMessage) {
      }

    };

    IRemoteTestListener l2 = new IRemoteTestListener() {

      @Override
      public void onStart(TestMessage tm) {
      }

      @Override
      public void onFinish(TestMessage tm) {
      }

      @Override
      public void onTestStart(TestResultMessage trm) {
      }

      @Override
      public void onTestSuccess(TestResultMessage trm) {
      }

      @Override
      public void onTestFailure(TestResultMessage trm) {
      }

      @Override
      public void onTestSkipped(TestResultMessage trm) {
      }

      @Override
      public void onTestFailedButWithinSuccessPercentage(TestResultMessage trm) {
      }

    };
    ResultXMLParser parser = new ResultXMLParser(l1, l2);
    String fileName = "/Users/cbeust/java/testng/test-output/testng-results.xml";
    parser.parse(fileName, new FileInputStream(new File(fileName)), false /* don't load classes */);
  }
}
