package org.testng.reporters;

import org.testng.ITestResult;

/** An interface that helps add custom xml tags to the TestNG generated xml report. */
public interface ICustomizeXmlReport {

  /**
   * @param xmlBuffer - An {@link XMLStringBuffer} object that represents the buffer to be used.
   * @param testResult - An {@link ITestResult} object that represents a test method's result.
   */
  void addCustomTagsFor(XMLStringBuffer xmlBuffer, ITestResult testResult);
}
