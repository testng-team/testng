package org.testng.xml;

import java.util.Map;
import java.util.Properties;
import org.testng.internal.Utils;
import org.testng.reporters.XMLStringBuffer;

public class XmlUtils {

  /**
   * Don't add this property if it's equal to its default value.
   *
   * @param p The properties
   * @param name The property name
   * @param value The property value
   * @param def The default value
   */
  public static void setProperty(Properties p, String name, String value, String def) {
    if (!def.equals(value) && value != null) {
      p.setProperty(name, value);
    }
  }

  public static void dumpParameters(XMLStringBuffer xsb, Map<String, String> parameters) {
    // parameters
    if (parameters.isEmpty()) {
      return;
    }
    for (Map.Entry<String, String> para : parameters.entrySet()) {
      Properties paramProps = new Properties();
      if (para.getKey() == null) {
        Utils.log("Skipping a null parameter.");
        continue;
      }
      if (para.getValue() == null) {
        String msg =
            String.format("Skipping parameter [%s] since it has a null value", para.getKey());
        Utils.log(msg);
        continue;
      }
      paramProps.setProperty("name", para.getKey());
      paramProps.setProperty("value", para.getValue());
      xsb.addEmptyElement("parameter", paramProps); // BUGFIX: TESTNG-27
    }
  }
}
