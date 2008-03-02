package org.testng.v6;

/**
 * Encapsulates a group that can have an after operation.
 */
public class RunGroup {
  public final static int XML_TEST = 1;
  public final static int CLASS = 2;
  public final static int GROUP = 3;

  private int m_type;
  private String m_name;
  private int m_id = 0;
  
  public RunGroup(int type, String name, int id) {
    m_type = type;
    m_name = name;
    m_id = id;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (obj == this) return true;
    try {
      RunGroup that = (RunGroup) obj;
      return that.getType() == getType() && that.getName().equals(getName());
    }
    catch(ClassCastException ex) {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return m_name.hashCode() ^ m_type;
  }

  public int getType() {
    return m_type;
  }

  public void setType(int type) {
    m_type = type;
  }

  public String getName() {
    return m_name;
  }

  public void setName(String name) {
    m_name = name;
  }

  public int getId() {
    return m_id;
  }

  public void setId(int id) {
    m_id = id;
  }
  
  public String toString() {
    String type = "";
    if (m_type == CLASS) type = "class:";
    else if (m_type == XML_TEST) type = "<test>:";
    else if (m_type == GROUP) type = "group:";

//        return "(RunGroup:" + m_id + ")";
    return "(RunGroup:" + m_id + " " + type + "\"" + m_name
      + "\")";
  }

}
