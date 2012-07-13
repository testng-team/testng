package org.testng.xml;

import org.testng.reporters.XMLStringBuffer;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

public class XmlUtils {

  /**
   * Don't add this property if it's equal to its default value.
   */
  public static void setProperty(Properties p, String name, String value, String def) {
    if (! def.equals(value) && value != null) {
      p.setProperty(name, value);
    }
  }

  public static void dumpParameters(XMLStringBuffer xsb, Map<String, String> parameters) {
    // parameters
    if (!parameters.isEmpty()) {
      for(Map.Entry<String, String> para: parameters.entrySet()) {
        Properties paramProps= new Properties();
        paramProps.setProperty("name", para.getKey());
        paramProps.setProperty("value", para.getValue());
        xsb.addEmptyElement("parameter", paramProps); // BUGFIX: TESTNG-27
      }
    }
  }

}
