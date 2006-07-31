package org.testng.xml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.testng.TestNG;
import org.testng.reporters.XMLStringBuffer;

/**
 * This class describes the tag <test>  in testng.xml.
 * 
 * @author <a href = "mailto:cedric&#64;beust.com">Cedric Beust</a>
 * @author <a href = 'mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 */
public class XmlTest implements Serializable, Cloneable {
  private XmlSuite m_suite = null;
  private String m_name = TestNG.DEFAULT_TEST_NAME;
  private Integer m_verbose = null;
  private Boolean m_isJUnit = null;
  private List<XmlClass> m_xmlClasses = new ArrayList<XmlClass>();
  
  private List<String> m_includedGroups = new ArrayList<String>();
  private List<String> m_excludedGroups = new ArrayList<String>();

  private Map<String, List<String>> m_metaGroups = new HashMap<String, List<String>>();
  private Map<String, String> m_parameters = new HashMap<String, String>();
  private Boolean m_isParallel = null;
  private String m_annotations = null;
  
  // BeanShell expression
  private String m_expression = null;
  private List<XmlMethodSelector> m_methodSelectors = new ArrayList<XmlMethodSelector>();
  // test level packages
  private List<XmlPackage> m_xmlPackages = new ArrayList<XmlPackage>();


  public XmlTest(XmlSuite suite) {
    m_suite = suite;
    m_suite.getTests().add(this);
  }

  public void setXmlPackages(List<XmlPackage> packages) {
    m_xmlPackages = packages;
  }
  
  public List<XmlPackage> getXmlPackages() {
    return m_xmlPackages;
  }
  
  public List<XmlMethodSelector> getMethodSelectors() {
    return m_methodSelectors;
  }

  public void setMethodSelectors(List<XmlMethodSelector> methodSelectors) {
    m_methodSelectors = methodSelectors;
  }
  
  public XmlSuite getSuite() {
    return m_suite;
  }
  
  /**
   * @return Returns the includedGroups.
   */
  public List<String> getIncludedGroups() {
    return m_includedGroups;
  }
  
  /**
   * @param classes The classes to set.
   */
  public void setClassNames(List<XmlClass> classes) {
    m_xmlClasses = classes;
  }
    
  /**
   * @return Returns the classes.
   */
  public List<XmlClass> getXmlClasses() {
    return m_xmlClasses;
  }
  
  public void setXmlClasses(List<XmlClass> classes) {
    m_xmlClasses = classes;
  }
  
  /**
   * @return Returns the name.
   */
  public String getName() {
    return m_name;
  }
  
  /**
   * @param name The name to set.
   */
  public void setName(String name) {
    m_name = name;
  }

  /**
   * @param i
   */
  public void setVerbose(int v) {
    m_verbose = new Integer(v);
  }
  
  /**
   * @param currentIncludedGroups
   */
  public void setIncludedGroups(List<String> g) {
    m_includedGroups = g;
  }

  /**
   * @param excludedGrousps The excludedGrousps to set.
   */
  public void setExcludedGroups(List<String> g) {
    m_excludedGroups = g;
  }

  /**
   * @return Returns the excludedGroups.
   */
  public List<String> getExcludedGroups() {
    return m_excludedGroups;
  }
  
  /**
   * @return Returns the verbose.
   */
  public int getVerbose() {
    Integer result = m_verbose;
    if (null == result) {
      result = m_suite.getVerbose();
    }
    
    return result.intValue();
  }
  /**
   * @return Returns the isJUnit.
   */
  public boolean isJUnit() {
    Boolean result = m_isJUnit;
    if (null == result) {
      result = m_suite.isJUnit();
    }
    
    return result;
  }

  /**
   * @param isJUnit The isJUnit to set.
   */
  public void setJUnit(boolean isJUnit) {
    m_isJUnit = isJUnit;
  }
  
  public void addMetaGroup(String name, List<String> metaGroup) {
    m_metaGroups.put(name, metaGroup);
  }

  /**
   * @return Returns the metaGroups.
   */
  public Map<String, List<String>> getMetaGroups() {
    return m_metaGroups;
  }
  
  /**
   * @param currentTestParameters
   */
  public void setParameters(Map parameters) {
    m_parameters = parameters;
  }
  
  public void addParameter(String key, String value) {
    m_parameters.put(key, value);
  }
  
  public String getParameter(String name) {
    String result = m_parameters.get(name);
    if (null == result) {
      result = m_suite.getParameter(name);
    }
    
    return result;
  }

  /**
   * Returns a merge of the the test parameters and its parent suite parameters. Test parameters 
   * have precedence over suite parameters.
   * 
   * @return a merge of the the test parameters and its parent suite parameters.
   */
  public Map<String, String> getParameters() {
    Map<String, String> result = new HashMap<String, String>();
    for (String key : getSuite().getParameters().keySet()) {
      result.put(key, getSuite().getParameters().get(key));
    }
    for (String key : m_parameters.keySet()) {
      result.put(key, m_parameters.get(key));
    }
    return result;
  }
    
  public void setParallel(boolean parallel) {
    m_isParallel = new Boolean(parallel);
  }
  
  public boolean isParallel() {
    boolean result = false;
    if (null != m_isParallel) {
      result = m_isParallel.booleanValue();
    }
    else {
      result = m_suite.isParallel();
    }
    
    return result;
  }
  
  public String getAnnotations() {
    String result = m_annotations;
    if (null != m_annotations) {
      result = m_annotations;
    }
    else {
      result = m_suite.getAnnotations();
    }
    
    return result;
  }
  
  public void setAnnotations(String annotations) {
    m_annotations = annotations;
  }

  public void setBeanShellExpression(String expression) {
    m_expression = expression;
  }
  
  public String getExpression() {
    return m_expression;
  }

  public String toXml(String indent) {
    XMLStringBuffer xsb = new XMLStringBuffer(indent);
    Properties p = new Properties();
    p.setProperty("name", getName());
    p.setProperty("junit", m_isJUnit != null ? m_isJUnit.toString() : "false");
    if (null != m_isParallel) {
      p.setProperty("parallel", "" + m_isParallel);
    }
    if (null != m_verbose) {
      p.setProperty("verbose", m_verbose.toString());
    }
    if (null != m_annotations) {
      p.setProperty("annotations", m_annotations);
    }
    
    xsb.push("test", p);
    
    
    if (null != getMethodSelectors() && !getMethodSelectors().isEmpty()) {
      xsb.push("method-selectors");
      for (XmlMethodSelector selector: getMethodSelectors()) {
        xsb.getStringBuffer().append(selector.toXml(indent + "    "));
      }
      
      xsb.pop("method-selectors");
    }
    
    // parameters
    if (!m_parameters.isEmpty()) {
      for (String paramName: m_parameters.keySet()) {
        Properties paramProps = new Properties();
        paramProps.setProperty("name", paramName);
        paramProps.setProperty("value", m_parameters.get(paramName));
      }
    }
    
    // groups
    if (!m_metaGroups.isEmpty() || !m_includedGroups.isEmpty() || !m_excludedGroups.isEmpty()) {
      xsb.push("groups");
      
      // define
      for (String metaGroupName: m_metaGroups.keySet()) {
        Properties metaGroupProp= new Properties();
        metaGroupProp.setProperty("name",  metaGroupName);
        
        xsb.push("define", metaGroupProp);
        
        for (String groupName: m_metaGroups.get(metaGroupName)) {
          Properties includeProps = new Properties();
          includeProps.setProperty("name", groupName);
          
          xsb.addEmptyElement("include", includeProps);
        }
        
        xsb.pop("define");
      }
      
      if (!m_includedGroups.isEmpty() || !m_excludedGroups.isEmpty()) {
        xsb.push("run");
        
        for (String includeGroupName: m_includedGroups) {
          Properties includeProps = new Properties();
          includeProps.setProperty("name", includeGroupName);
          
          xsb.addEmptyElement("include", includeProps);
        }
        
        for (String excludeGroupName: m_excludedGroups) {
          Properties excludeProps = new Properties();
          excludeProps.setProperty("name", excludeGroupName);
          
          xsb.addEmptyElement("exclude", excludeProps);
        }
        
        xsb.pop("run");
      }
      
      xsb.pop("groups");
    }
    
    
    // HINT: don't call getXmlPackages() cause you will retrieve the suite packages too
    if (null != m_xmlPackages && !m_xmlPackages.isEmpty()) {
      xsb.push("packages");
      
      for (XmlPackage pack: m_xmlPackages) {
        xsb.getStringBuffer().append(pack.toXml("  "));
      }
      
      xsb.pop("packages");
    }
    
    // classes
    if (null != getXmlClasses() && !getXmlClasses().isEmpty()) {
      xsb.push("classes");
      for (XmlClass cls : getXmlClasses()) {
        xsb.getStringBuffer().append(cls.toXml(indent + "    "));
      }
      xsb.pop("classes");
    }
    
    xsb.pop("test");
    
    return xsb.toXML();
  }
  
  @Override
  public String toString() {
    StringBuffer result = new StringBuffer("[Test: \"" + m_name + "\"")
      .append(" verbose:" + m_verbose);
    
    result.append("[parameters:");
    for (String k : m_parameters.keySet()) {
      String v = m_parameters.get(k);
      result.append(k + "=>" + v);
    }
    
    result.append("]");
    result.append("[metagroups:");
    for (String g : m_metaGroups.keySet()) {
      List<String> mg = m_metaGroups.get(g);
      result.append(g).append("=");
      for (String n : mg) {
        result.append(n).append(",");
      }
    }
    result.append("] ");
    
    result.append("[included: ");
    for (String g : m_includedGroups) {
      result.append(g).append(" ");
    }
    result.append("]");
    
    result.append("[excluded: ");
    for (String g : m_excludedGroups) {
      result.append(g).append("");
    }
    result.append("] ");
    
    result.append(" classes:");
    for (XmlClass cl : m_xmlClasses) {
      result.append(cl).append(" ");
    }
    
    result.append("] ");
    
    return result.toString();
  }
  
  static void ppp(String s) {
    System.out.println("[XmlTest] " + s);
  }
  
  /**
   * Clone the <TT>source</TT> <CODE>XmlTest</CODE> by including: 
   * - test attributes
   * - groups definitions
   * - parameters
   * 
   * The &lt;classes&gt; subelement is ignored for the moment.
   * 
   * @param suite
   * @param source
   * @return
   */
  @Override
  public Object clone() {
    XmlTest result = new XmlTest(getSuite());
    
    result.setName(getName());
    result.setAnnotations(getAnnotations());
    result.setIncludedGroups(getIncludedGroups());
    result.setExcludedGroups(getExcludedGroups());
    result.setJUnit(isJUnit());
    result.setParallel(isParallel());
    result.setVerbose(getVerbose());
    result.setParameters(getParameters());
    result.setXmlPackages(getXmlPackages());
    
    Map<String, List<String>> metagroups = getMetaGroups();
    for (String groupName: metagroups.keySet()) {
      result.addMetaGroup(groupName, metagroups.get(groupName));
    }
    
    return result;
  }
  
}
