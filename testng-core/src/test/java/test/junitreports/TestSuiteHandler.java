package test.junitreports;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class TestSuiteHandler extends DefaultHandler {
  private static final List<String> tags = Arrays.asList("error", "skipped", "ignored", "failure");
  private final Testsuite testsuite = new Testsuite();
  private final Deque<String> elementStack = new ArrayDeque<>();
  private final Deque<Testcase> testcaseStack = new ArrayDeque<>();

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) {
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
  public void endElement(String uri, String localName, String qName) {
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
