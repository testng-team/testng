package org.testng.reporters;

import java.util.Iterator;
import java.util.Properties;
import java.util.Map.Entry;


/**
 * Static helpers for XML.
 * 
 * @author Cedric Beust Jul 21, 2003
 *
 */
public final class XMLUtils {

  /** Platform specific end of line */
  private static final String EOL = System.getProperty("line.separator");
  
  private XMLUtils() {
    // Hide constructor
  }
  
  static public String xml(String indent, String elementName, String content, Properties attributes) {
    StringBuffer result = new StringBuffer();
    xmlOpen(result, indent, elementName, attributes, true /* no newline */);
    result.append(content);
    xmlClose(result, "", elementName);
    
    return result.toString();
  }
  public static void xmlOptional(StringBuffer result, String sp,
                                                      String elementName, Boolean value, Properties attributes)
  {
    if (null != value) {
      xmlRequired(result, sp, elementName, value.toString(), attributes);
    }
  }
  public static void xmlOptional(StringBuffer result, String sp,
                                                      String elementName, String value, Properties attributes)
  {
    if (null != value) {
      xmlRequired(result, sp, elementName, value, attributes);
    }
  }
  public static void xmlRequired(StringBuffer result, String sp,
                                                       String elementName, String value, Properties attributes)
  {
    result.append(xml(sp, elementName, value, attributes));
  }
  
  public static void xmlOpen(StringBuffer result, String indent,
      String tag, Properties attributes)
  {
    xmlOpen(result, indent, tag, attributes, false /* no newline */);
  }
  
  /**
   * Appends the attributes to result. The attributes are added on a single line
   * as: key1="value1" key2="value2" ... (a space is added before the first key) 
   *
   * @param result the buffer to append attributes to.
   * @param attributes the attributes to append (may be null).
   */
  public static void appendAttributes(StringBuffer result, Properties attributes) {
    if (null != attributes) {
      for (Iterator it = attributes.entrySet().iterator(); it.hasNext(); ) {
        Entry entry = (Entry) it.next();
        String key = entry.getKey().toString();
        String value = entry.getValue().toString();
        result.append(" ").append(key).append("=\"").append(value).append("\"");
      }
    }
  }
  
  public static void xmlOpen(StringBuffer result, String indent,
                                                 String tag, Properties attributes,
                                                 boolean noNewLine)
  {
    result.append(indent).append("<").append(tag);
    appendAttributes(result, attributes);
    result.append(">");
    if (! noNewLine) result.append(EOL);
  }

  public static void xmlClose(StringBuffer result, String indent,
                                                 String tag)
  {
    result.append(indent).append("</").append(tag).append(">").append(EOL);
  }
}
