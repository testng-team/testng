package test.junitreports;

import java.util.LinkedList;
import java.util.List;
import org.xml.sax.Attributes;

public class Testsuite {

  public String getName() {
    return name;
  }

  public int getTests() {
    return tests;
  }

  public int getIgnored() {
    return ignored;
  }

  public int getFailures() {
    return failures;
  }

  public int getSkipped() {
    return skipped;
  }

  public int getErrors() {
    return errors;
  }

  public List<Testcase> getTestcase() {
    return testcase;
  }

  public void addTestcase(Testcase testcase) {
    this.testcase.add(testcase);
  }

  public void init(Attributes attributes) {
    String value = attributes.getValue("name");
    if (value != null) {
      this.name = value;
    }
    value = attributes.getValue("tests");
    if (value != null) {
      this.tests = Integer.parseInt(value);
    }
    value = attributes.getValue("ignored");
    if (value != null) {
      this.ignored = Integer.parseInt(value);
    }
    value = attributes.getValue("failures");
    if (value != null) {
      this.failures = Integer.parseInt(value);
    }
    value = attributes.getValue("skipped");
    if (value != null) {
      this.skipped = Integer.parseInt(value);
    }
    value = attributes.getValue("errors");
    if (value != null) {
      this.errors = Integer.parseInt(value);
    }
  }

  @Override
  public String toString() {
    return "Testsuite{"
        + "name='"
        + name
        + '\''
        + ", tests="
        + tests
        + ", ignored="
        + ignored
        + ", failures="
        + failures
        + ", skipped="
        + skipped
        + ", errors="
        + errors
        + ", testcase="
        + testcase
        + '}';
  }

  private String name;
  private int tests;
  private int ignored;
  private int failures;
  private int skipped;
  private int errors;
  private List<Testcase> testcase = new LinkedList<>();
}
