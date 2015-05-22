package org.testng.internal;

import org.testng.collections.Maps;
import org.testng.xml.XmlClass;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClassInfoMap {
  private Map<Class<?>, XmlClass> m_map = Maps.newLinkedHashMap();
  private boolean includeNestedClasses;

  public ClassInfoMap() {
  }

  public ClassInfoMap(List<XmlClass> classes) {
    this(classes, true);
  }

  public ClassInfoMap(List<XmlClass> classes, boolean includeNested) {
    includeNestedClasses = includeNested;
    for (XmlClass xmlClass : classes) {
      try {
        Class c = xmlClass.getSupportClass();
        registerClass(c, xmlClass);
      } catch (NoClassDefFoundError e) {
        Utils.log("[ClassInfoMap]", 1, "Unable to open class " + xmlClass.getName()
            + " - unable to resolve class reference " + e.getMessage());
        if (xmlClass.loadClasses()) {
          throw e;
        }
      }
    }
  }

  private void registerClass(Class cl, XmlClass xmlClass) {
    m_map.put(cl, xmlClass);
    if (includeNestedClasses) {
      for (Class c : cl.getClasses()) {
        if (! m_map.containsKey(c)) registerClass(c, xmlClass);
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
