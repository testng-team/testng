package org.testng.internal;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.collections.Maps;
import org.testng.xml.XmlClass;

public class ClassInfoMap {
  private Map<Class<?>, XmlClass> m_map = Maps.newHashMap();

  public ClassInfoMap() {
  }

  public ClassInfoMap(List<XmlClass> classes) {
    for (XmlClass xmlClass : classes) {
      try {
        m_map.put(xmlClass.getSupportClass(), xmlClass);
      }
      catch (NoClassDefFoundError e) {
        Utils.log("[ClassInfoMap]", 1, "Unable to open class " + xmlClass.getName()
            + " - unable to resolve class reference " + e.getMessage());
        if (xmlClass.getDeclaredClass() == Boolean.TRUE) {
          throw e;
        }
      }
    }
  }

  public void addClass(Class<?> cls) {
    m_map.put(cls, null);
  }

  public XmlClass getXmlClass(Class<?> cls) {
    return m_map.get(cls);
  }

  public void put(Class<?> cls, XmlClass xmlClass) {
    m_map.put(cls, xmlClass);
  }

  public Set<Class<?>> getClasses() {
    return m_map.keySet();
  }

  public int getSize() {
    return m_map.size();
  }
}
