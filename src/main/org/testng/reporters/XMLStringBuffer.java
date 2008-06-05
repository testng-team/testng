package org.testng.reporters;

import java.util.Properties;
import java.util.Stack;

/**
 * This class allows you to generate an XML text document by pushing
 * and popping tags from a stack maintained internally.
 * 
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a> Jul 21, 2003
 */
public class XMLStringBuffer {
  /** End of line */
  private static final String EOL = System.getProperty("line.separator");
  
  /** Tab space indent for XML document */
  private static final String DEFAULT_INDENT_INCREMENT = "  ";
  
  /** The buffer to hold the xml document */
  private final StringBuffer m_buffer;
  
  /** The stack of tags to make sure XML document is well formed. */
  private final Stack<Tag> m_tagStack = new Stack<Tag>();

  /** A string of space character representing the current indentation. */
  private String m_currentIndent = "";
  
  /**
   * 
   * @param start A string of spaces indicating the indentation at which
   * to start the generation.
   */
  public XMLStringBuffer(String start) {
    this(new StringBuffer(), start);
  }

  /**
   * Set the xml version and encoding for this document.
   * 
   * @param v the XML version
   * @param enc the XML encoding
   */
  public void setXmlDetails(String v, String enc) {
    m_buffer.append("<?xml version=\"" + v + "\" encoding=\"" + enc + "\"?>").append(EOL);
  }
  
  /**
   * Set the doctype for this document.
   * 
   * @param docType The DOCTYPE string, without the "&lt;!DOCTYPE " "&gt;"
   */
  public void setDocType(String docType) {
    m_buffer.append("<!DOCTYPE " + docType + ">" + EOL);
  }
  
  
  /**
   * 
   * @param buffer The StringBuffer to use internally to represent the
   * document.
   * @param start A string of spaces indicating the indentation at which
   * to start the generation.
   */
  public XMLStringBuffer(StringBuffer buffer, String start) {
    m_buffer = buffer;
    m_currentIndent = start;
  }
  
  
  /**
   * Push a new tag.  Its value is stored and will be compared against the parameter
   * passed to pop().
   * 
   * @param tagName The name of the tag.
   * @param schema The schema to use (can be null or an empty string).
   * @param attributes A Properties file representing the attributes (or null)
   */
  public void push(String tagName, String schema, Properties attributes) {
    XMLUtils.xmlOpen(m_buffer, m_currentIndent, tagName + schema, attributes);
    m_tagStack.push(new Tag(m_currentIndent, tagName));
    m_currentIndent += DEFAULT_INDENT_INCREMENT;
  }

  /**
   * Push a new tag.  Its value is stored and will be compared against the parameter
   * passed to pop().
   * 
   * @param tagName The name of the tag.
   * @param schema The schema to use (can be null or an empty string).
   */
  public void push(String tagName, String schema) {
    push(tagName, schema, null);
  }
  
  /**
   * Push a new tag.  Its value is stored and will be compared against the parameter
   * passed to pop().
   * 
   * @param tagName The name of the tag.
   * @param attributes A Properties file representing the attributes (or null)
   */
  public void push(String tagName, Properties attributes) {
    push(tagName, "", attributes);
  }
  
  /**
   * Push a new tag.  Its value is stored and will be compared against the parameter
   * passed to pop().
   * 
   * @param tagName The name of the tag.
   */
  public void push(String tagName) {
    push(tagName, "");
  }
    
  /**
   * Pop the last pushed element without verifying it if matches the previously
   * pushed tag.
   */
  public void pop() {
    pop(null);
  }
  
  /**
   * Pop the last pushed element and throws an AssertionError if it doesn't
   * match the corresponding tag that was pushed earlier.
   * 
   * @param tagName The name of the tag this pop() is supposed to match.
   */
  public void pop(String tagName) {
    m_currentIndent = m_currentIndent.substring(DEFAULT_INDENT_INCREMENT.length());
    Tag t = m_tagStack.pop();
    if (null != tagName) {
      if (!tagName.equals(t.tagName)) {
        // TODO Is it normal to throw an Error here?
        throw new AssertionError(
            "Popping the wrong tag: " + t.tagName + " but expected " + tagName);
      }
    }
    XMLUtils.xmlClose(m_buffer, m_currentIndent, t.tagName);
  }
  
  /**
   * Add a required element to the current tag.  An opening and closing tag
   * will be generated even if value is null.
   * @param tagName The name of the tag
   * @param value The value for this tag
   */
  public void addRequired(String tagName, String value) {
    addRequired(tagName, value, null);
  }

  /**
   * Add a required element to the current tag.  An opening and closing tag
   * will be generated even if value is null.
   * @param tagName The name of the tag
   * @param value The value for this tag
   * @param attributes A Properties file containing the attributes (or null)
   */
  public void addRequired(String tagName, String value, Properties attributes) {
    XMLUtils.xmlRequired(m_buffer, m_currentIndent, tagName, value, attributes);
  }

  /**
   * Add an optional String element to the current tag.  If value is null, nothing is
   * added.
   * @param tagName The name of the tag
   * @param value The value for this tag
   * @param attributes A Properties file containing the attributes (or null)
   */
  public void addOptional(String tagName, String value, Properties attributes) {
    XMLUtils.xmlOptional(m_buffer, m_currentIndent, tagName, value, attributes);
  }
  
  /**
   * Add an optional String element to the current tag.  If value is null, nothing is
   * added.
   * @param tagName The name of the tag
   * @param value The value for this tag
   */
  public void addOptional(String tagName, String value) {
    addOptional(tagName, value, null);
  }
  
  /**
   * Add an optional Boolean element to the current tag.  If value is null, nothing is
   * added.
   * @param tagName The name of the tag
   * @param value The value for this tag
   * @param attributes A Properties file containing the attributes (or null)
   */
  public void addOptional(String tagName, Boolean value, Properties attributes) {
    if (null != value) {
      XMLUtils.xmlOptional(m_buffer, m_currentIndent, tagName, value.toString(), attributes);
    }
  }
  
  /**
   * Add an optional Boolean element to the current tag.  If value is null, nothing is
   * added.
   * @param tagName The name of the tag
   * @param value The value for this tag
   * @param attributes A Properties file containing the attributes (or null)
   */
  public void addOptional(String tagName, Boolean value) {
    addOptional(tagName, value, null);
  }

  /**
   * Add an empty element tag (e.g. <foo/>)
   * 
   * @param tagName The name of the tag
   * 
   */
  public void addEmptyElement(String tagName) {
    addEmptyElement(tagName, null);
  }
  
  /**
   * Add an empty element tag (e.g. <foo/>)
   * @param tagName The name of the tag
   * @param attributes A Properties file containing the attributes (or null)
   */
  public void addEmptyElement(String tagName, Properties attributes) {
    m_buffer.append(m_currentIndent).append("<").append(tagName);
    XMLUtils.appendAttributes(m_buffer, attributes);
    m_buffer.append("/>").append(EOL);
  }
  
  private static void ppp(String s) {
    System.out.println("[XMLStringBuffer] " + s);
  }
  
  /**
   * Add a CDATA tag.
   */
  public void addCDATA(String content) {
    if (content.indexOf("]]>") > -1) {
      String[] subStrings = content.split("]]>");
      m_buffer.append(m_currentIndent).append("<![CDATA[").append(subStrings[0]).append("]]]]>");
      for (int i = 1; i < subStrings.length - 1; i++) {
        m_buffer.append("<![CDATA[>").append(subStrings[i]).append("]]]]>");
      }
      m_buffer.append("<![CDATA[>").append(subStrings[subStrings.length - 1]).append("]]>");
      if (content.endsWith("]]>")) {
        m_buffer.append("<![CDATA[]]]]>").append("<![CDATA[>]]>");
      }
      m_buffer.append(EOL);
    } else {
      m_buffer.append(m_currentIndent).append("<![CDATA[").append(content).append("]]>" + EOL);
    }
  }
  
  /**
   * 
   * @return The StringBuffer used to create the document.
   */
  public StringBuffer getStringBuffer() {
    return m_buffer;
  }
  
  /**
   * 
   * @return The String representation of the XML for this XMLStringBuffer.
   */
  public String toXML() {
    return m_buffer.toString();
  }
  
  public static void main(String[] argv) {
    StringBuffer result = new StringBuffer();
    XMLStringBuffer sb = new XMLStringBuffer(result, "");
    
    sb.push("family");
    Properties p = new Properties();
    p.setProperty("prop1", "value1");
    p.setProperty("prop2", "value2");
    sb.addRequired("cedric", "true", p);
    sb.addRequired("alois", "true");
    sb.addOptional("anne-marie", (String) null);
    sb.pop();
    
    System.out.println(result.toString());
    
    assert ("<family>" + EOL + "<cedric>true</cedric>" + EOL + "<alois>true</alois>" + EOL + "</family>"  + EOL)
      .equals(result.toString());
  }
}


////////////////////////

class Tag {
  public final String tagName;
  public final String indent;
  
  public Tag(String ind, String n) {
    tagName = n;
    indent = ind;
  }
}
