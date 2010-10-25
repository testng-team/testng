package org.testng.internal;


/**
 * Describes a property
 *
 * @author Cedric Beust, May 2, 2004
 *
 */
public class TestNGProperty {
  private String m_commandLineName = null;
  private String m_name = null;
  private String m_documentation = null;
  private String m_default = null;

  public TestNGProperty(String clName, String name, String doc, String def) {
    init(clName, name, doc, def);
  }

  public TestNGProperty(String name, String doc, String def) {
    init(name, name, doc, def);
  }

  private void init(String clName, String name, String doc, String def) {
    m_commandLineName = clName;
    m_name = name;
    m_documentation = doc;
    m_default = def;
  }

  /**
   * @return Returns the default.
   */
  public String getDefault() {
    return m_default;
  }
  /**
   * @return Returns the documentation.
   */
  public String getDocumentation() {
    return m_documentation;
  }
  /**
   * @return Returns the name.
   */
  public String getName() {
    return m_name;
  }

  public String getCommandLineName() {
    return m_commandLineName;
  }
}
