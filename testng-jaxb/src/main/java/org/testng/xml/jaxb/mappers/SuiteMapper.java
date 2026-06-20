package org.testng.xml.jaxb.mappers;

import org.testng.xml.XmlSuite;
import org.testng.xml.jaxb.Suite;

public class SuiteMapper {

  public static XmlSuite toXmlSuite(Suite suite) {
    XmlSuite xmlSuite = new XmlSuite();
    xmlSuite.setName(suite.getName());
    if (suite.getVerbose() != null) {
      xmlSuite.setVerbose(Integer.parseInt(suite.getVerbose()));
    }
    if (suite.getParallel() != null) {
      xmlSuite.setParallel(XmlSuite.ParallelMode.getValidParallel(suite.getParallel()));
    }
    if (suite.getThreadCount() != null) {
      xmlSuite.setThreadCount(Integer.parseInt(suite.getThreadCount()));
    }
    if (suite.getDataProviderThreadCount() != null) {
      xmlSuite.setDataProviderThreadCount(Integer.parseInt(suite.getDataProviderThreadCount()));
    }
    if (suite.getConfigfailurepolicy() != null) {
      xmlSuite.setConfigFailurePolicy(
          XmlSuite.FailurePolicy.getValidPolicy(suite.getConfigfailurepolicy()));
    }
    if (suite.getGroupByInstances() != null) {
      xmlSuite.setGroupByInstances(Boolean.parseBoolean(suite.getGroupByInstances()));
    }
    if (suite.getAllowReturnValues() != null) {
      xmlSuite.setAllowReturnValues(Boolean.parseBoolean(suite.getAllowReturnValues()));
    }
    xmlSuite.setTimeOut(suite.getTimeOut());

    for (Object o : suite.getListenersOrPackagesOrTest()) {
      if (o instanceof org.testng.xml.jaxb.Listeners) {
        xmlSuite.setListeners(ListenersMapper.toXmlListeners((org.testng.xml.jaxb.Listeners) o));
      } else if (o instanceof org.testng.xml.jaxb.Packages) {
        xmlSuite.setPackages(PackagesMapper.toXmlPackages((org.testng.xml.jaxb.Packages) o));
      } else if (o instanceof org.testng.xml.jaxb.Test) {
        xmlSuite.addTest(TestMapper.toXmlTest((org.testng.xml.jaxb.Test) o));
      } else if (o instanceof org.testng.xml.jaxb.Parameter) {
        xmlSuite
            .getParameters()
            .put(
                ((org.testng.xml.jaxb.Parameter) o).getName(),
                ((org.testng.xml.jaxb.Parameter) o).getValue());
      } else if (o instanceof org.testng.xml.jaxb.MethodSelectors) {
        xmlSuite.setMethodSelectors(
            MethodSelectorsMapper.toXmlMethodSelectors((org.testng.xml.jaxb.MethodSelectors) o));
      } else if (o instanceof org.testng.xml.jaxb.SuiteFiles) {
        xmlSuite.setSuiteFiles(
            SuiteFilesMapper.toXmlSuiteFiles((org.testng.xml.jaxb.SuiteFiles) o));
      }
    }

    return xmlSuite;
  }
}
