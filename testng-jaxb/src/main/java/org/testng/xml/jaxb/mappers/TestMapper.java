package org.testng.xml.jaxb.mappers;

import org.testng.xml.XmlTest;
import org.testng.xml.jaxb.Test;

public class TestMapper {

  public static XmlTest toXmlTest(Test test) {
    XmlTest xmlTest = new XmlTest();
    xmlTest.setName(test.getName());
    if (test.getVerbose() != null) {
      xmlTest.setVerbose(Integer.parseInt(test.getVerbose()));
    }
    if (test.getSkipfailedinvocationcounts() != null) {
      xmlTest.setSkipFailedInvocationCounts(
          Boolean.parseBoolean(test.getSkipfailedinvocationcounts()));
    }
    if (test.getPreserveOrder() != null) {
      xmlTest.setPreserveOrder(Boolean.parseBoolean(test.getPreserveOrder()));
    }
    if (test.getGroupByInstances() != null) {
      xmlTest.setGroupByInstances(Boolean.parseBoolean(test.getGroupByInstances()));
    }
    if (test.getAllowReturnValues() != null) {
      xmlTest.setAllowReturnValues(Boolean.parseBoolean(test.getAllowReturnValues()));
    }
    xmlTest.setParameters(ParameterMapper.toXmlParameters(test.getParameter()));
    return xmlTest;
  }
}
