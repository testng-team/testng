package org.testng.xml;

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

}
