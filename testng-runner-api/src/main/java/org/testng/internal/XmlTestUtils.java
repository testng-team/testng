package org.testng.internal;

import java.util.Map;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlTest;

final class XmlTestUtils {

  private XmlTestUtils() {}

  static Map<String, String> findMethodParameters(
      XmlTest test, String className, String methodName) {
    Map<String, String> result = test.getAllParameters();
    for (XmlClass xmlClass : test.getXmlClasses()) {
      if (xmlClass.getName().equals(className)) {
        result.putAll(xmlClass.getLocalParameters());
        for (XmlInclude include : xmlClass.getIncludedMethods()) {
          if (include.getName().equals(methodName)) {
            result.putAll(include.getLocalParameters());
            break;
          }
        }
      }
    }

    return result;
  }
}
