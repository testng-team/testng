package org.testng.reporters;

import java.util.Properties;
import java.util.Stack;

/**
 * This class allows you to generate an XML text document by pushing
 * and popping tags from a stack maintained internally.
 * 
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a> Jul 21, 2003
 *
 */
public class XMLStringBuffer {
  private StringBuffer m_buffer;
  private Stack<Tag> m_tagStack = new Stack<Tag>();
  private String m_defaultIndentIncrement = "  ";
  private String m_defaultStart = "";
  private String m_currentIndent = "";
  
  /**
   * 
   * @param start A string of spaces indicating the indentation at which
   * to start the generation.
   */
  public XMLStringBuffer(String start) {
    init(new StringBuffer(), start);
  }
  
  /**
   * Set the doctype for this document.
   * 
   * @param docType The DOCTYPE, without the "!DOCTYPE " string
   */
  public void setDocType(String docType) {
    m_buffer.append("<!DOCTYPE " + docType + ">\n");
  }
  
  /**
   * 
   * @param buffer The StringBuffer to use internally to represent the
   * document.
   * @param start A string of spaces indicating the indentation at which
   * to start the generation.
   */
  public XMLStringBuffer(StringBuffer buffer, String start) {
    init(buffer, start);
  }
  
  private void init(StringBuffer buffer, String start) {
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
    m_currentIndent += m_defaultIndentIncrement;
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
  public void pop(String tagName) throws AssertionError {
    m_currentIndent = m_currentIndent.substring(0, m_currentIndent.length() - m_defaultIndentIncrement.length());
    Tag t = (Tag) m_tagStack.pop();
    if (null != tagName) {
      if (! tagName.equals(t.tagName)) {
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
    m_buffer.append("/>\n");
  }
  
  private static void ppp(String s) {
    System.out.println("[XMLStringBuffer] " + s);
  }
  
  /**
   * Add a CDATA tag.
   */
  public void addCDATA(String content) {
    m_buffer.append(m_currentIndent).append("<![CDATA[").append(content).append("]]>\n");
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
    
    assert "<family>\n<cedric>true</cedric>\n<alois>true</alois>\n</family>\n"
      .equals(result.toString());
  }
}


////////////////////////

class Tag {
  public String tagName;
  public String indent;
  
  public Tag(String ind, String n) {
    tagName = n;
    indent = ind;
  }
}
