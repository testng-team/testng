package org.testng.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.testng.reporters.XMLStringBuffer;

/**
 * This class describes the tag <package>  in testng.xml.
 * 
 * @author Cedric
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 */
public class XmlPackage {
  private String m_name;
  private List<String> m_include = new ArrayList<String>();
  private List<String> m_exclude = new ArrayList<String>();
  
  /**
   * @return the exclude
   */
  public List<String> getExclude() {
    return m_exclude;
  }
  
  /**
   * @param exclude the exclude to set
   */
  public void setExclude(List<String> exclude) {
    m_exclude = exclude;
  }
  
  /**
   * @return the include
   */
  public List<String> getInclude() {
    return m_include;
  }
  
  /**
   * @param include the include to set
   */
  public void setInclude(List<String> include) {
    m_include = include;
  }
  
  /**
   * @return the name
   */
  public String getName() {
    return m_name;
  }
  
  /**
   * @param name the name to set
   */
  public void setName(String name) {
    m_name = name;
  }
  
  public Object toXml(String indent) {
    XMLStringBuffer xsb = new XMLStringBuffer(indent);
    Properties p = new Properties();
    p.setProperty("name", getName());
    
    xsb.push("package", p);
    
    for (String m : getInclude()) {
      Properties includeProp= new Properties();
      includeProp.setProperty("name", m);
      xsb.addEmptyElement("include", includeProp);
    }
    for (String m: getExclude()) {
      Properties excludeProp= new Properties();
      excludeProp.setProperty("name", m);
      xsb.addEmptyElement("exclude", excludeProp);
    }
    
    xsb.pop("package");

    return xsb.toXML();
  }
}
