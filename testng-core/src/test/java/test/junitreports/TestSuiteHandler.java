package test.junitreports;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class TestSuiteHandler extends DefaultHandler {
  private static List<String> tags = Arrays.asList("error", "skipped", "ignored", "failure");
  private Testsuite testsuite = new Testsuite();
  private Stack<String> elementStack = new Stack();
  private Stack<Testcase> testcaseStack = new Stack<>();

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes)
      throws SAXException {
    this.elementStack.push(qName);
    if ("testsuite".equals(qName)) {
      if (attributes != null) {
        testsuite.init(attributes);
      }
    }
    if ("testcase".equals(qName)) {
      Testcase testcase = new Testcase();
      if (attributes != null) {
        testcase.init(attributes);
      }
      testcaseStack.push(testcase);
    }
    if (tags.contains(qName)) {
      Testcase testcase = testcaseStack.pop();
      String innerTag = qName;
      testcase.setInnerTagType(innerTag);
      testcaseStack.push(testcase);
    }
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    elementStack.pop();
    if ("testcase".equals(qName)) {
      Testcase testcase = testcaseStack.pop();
      testsuite.addTestcase(testcase);
    }
  }

  public Testsuite getTestsuite() {
    return testsuite;
  }
}
