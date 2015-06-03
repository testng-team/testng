package org.testng.reporters;

/**
 * interface groups the XML constants
 * tries to emulate what's in org.apache.tools.ant.taskdefs.optional.junit.XMLConstants
 * to be compatible with junitreport
 */
public interface XMLConstants {
  /** the testsuites element for the aggregate document */
  String TESTSUITES = "testsuites";

  /** the testsuite element */
  String TESTSUITE = "testsuite";

  /** the testcase element */
  String TESTCASE = "testcase";

  /** the error element */
  String ERROR = "error";

  /** the failure element */
  String FAILURE = "failure";

  /** the system-err element */
  String SYSTEM_ERR = "system-err";

  /** the system-out element */
  String SYSTEM_OUT = "system-out";

  /** package attribute for the aggregate document */
  String ATTR_PACKAGE = "package";

  /** name attribute for property, testcase and testsuite elements */
  String ATTR_NAME = "name";

  /** time attribute for testcase and testsuite elements */
  String ATTR_TIME = "time";

  /** errors attribute for testsuite elements */
  String ATTR_ERRORS = "errors";

  /** failures attribute for testsuite elements */
  String ATTR_FAILURES = "failures";

  /** tests attribute for testsuite elements */
  String ATTR_TESTS = "tests";

  /** type attribute for failure and error elements */
  String ATTR_TYPE = "type";

  /** message attribute for failure elements */
  String ATTR_MESSAGE = "message";

  /** the properties element */
  String PROPERTIES = "properties";

  /** the property element */
  String PROPERTY = "property";

  /** value attribute for property elements */
  String ATTR_VALUE = "value";

  /** classname attribute for testcase elements */
  String ATTR_CLASSNAME = "classname";

  String ATTR_HOSTNAME = "hostname";

  String ATTR_TIMESTAMP = "timestamp";
}