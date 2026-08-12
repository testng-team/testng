package org.testng.xml;

import java.util.Map;
import java.util.Properties;
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

  /**
   * @deprecated Moved to {@code DefaultXmlWeaver}. This class is the one place in {@code
   *     org.testng.xml} that still mentions {@link XMLStringBuffer}, because the buffer is the
   *     parameter type: dropping the method would be a hard break of a public signature, and this
   *     module has no binary compatibility check in CI to measure the fallout.
   */
  @Deprecated
  public static void dumpParameters(XMLStringBuffer xsb, Map<String, String> parameters) {
    DefaultXmlWeaver.dumpParameters(xsb, parameters);
  }
}
