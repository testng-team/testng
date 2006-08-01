package org.testng.reporters;

import java.util.Iterator;
import java.util.Properties;


/**
 * Static helpers for XML.
 * 
 * @author Cedric Beust Jul 21, 2003
 *
 */
public class XMLUtils {

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
  
  public static void appendAttributes(StringBuffer result, Properties attributes) {
    if (null != attributes) {
      for (Iterator it = attributes.keySet().iterator(); it.hasNext(); ) {
        String key = it.next().toString();
        String value = attributes.getProperty(key);
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
    if (! noNewLine) result.append("\n");
  }

  public static void xmlClose(StringBuffer result, String indent,
                                                 String tag)
  {
    result.append(indent).append("</").append(tag).append(">\n");
  }
}
