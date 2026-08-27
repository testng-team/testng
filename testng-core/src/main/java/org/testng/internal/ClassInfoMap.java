package org.testng.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.testng.xml.XmlClass;

public class ClassInfoMap {

  // Every <class> tag naming this class, in the order they appear in the <test>: the tag can be
  // repeated, and each occurrence carries its own parameters and its own <methods>.
  private final Map<Class<?>, List<XmlClass>> m_map = new LinkedHashMap<>();
  // The classes a <class> tag names outright, as opposed to the nested ones registered under their
  // outer class's tag.
  private final Set<Class<?>> m_named = new HashSet<>();
  private final boolean includeNestedClasses;

  public ClassInfoMap() {
    this(Collections.emptyList(), false);
  }

  public ClassInfoMap(List<XmlClass> classes) {
    this(classes, true);
  }

  public ClassInfoMap(List<XmlClass> classes, boolean includeNested) {
    includeNestedClasses = includeNested;
    for (XmlClass xmlClass : classes) {
      try {
        Class<?> c = xmlClass.getSupportClass();
        registerClass(c, xmlClass);
      } catch (NoClassDefFoundError e) {
        Utils.log(
            "[ClassInfoMap]",
            1,
            "Unable to open class "
                + xmlClass.getName()
                + " - unable to resolve class reference "
                + e.getMessage());
        if (xmlClass.loadClasses()) {
          throw e;
        }
      }
    }
  }

  private void registerClass(Class<?> cl, XmlClass xmlClass) {
    List<XmlClass> occurrences = m_map.computeIfAbsent(cl, key -> new ArrayList<>());
    if (m_named.add(cl)) {
      // The first tag to name this class replaces whatever an enclosing class registered on its
      // behalf below: a nested class listed in its own right is described by its own tag, not by
      // its outer class's. Later tags naming it are further occurrences and stack. Cleared in
      // place rather than re-keyed: the map's iteration order is the order the classes run in.
      occurrences.clear();
    }
    occurrences.add(xmlClass);
    registerNestedClassesOf(cl, xmlClass);
  }

  /** Attributes a class's nested classes to its tag, for the ones no tag of their own names. */
  private void registerNestedClassesOf(Class<?> cl, XmlClass xmlClass) {
    if (!includeNestedClasses) {
      return;
    }
    for (Class<?> c : cl.getClasses()) {
      if (!m_map.containsKey(c)) {
        m_map.computeIfAbsent(c, key -> new ArrayList<>()).add(xmlClass);
        registerNestedClassesOf(c, xmlClass);
      }
    }
  }

  public void addClass(Class<?> cls) {
    m_map.computeIfAbsent(cls, key -> new ArrayList<>());
  }

  /**
   * @return the last {@code <class>} tag registered for this class, which is what a caller that
   *     cannot express more than one occurrence gets. Use {@link #getXmlClasses(Class)} to see them
   *     all.
   */
  public @Nullable XmlClass getXmlClass(Class<?> cls) {
    List<XmlClass> xmlClasses = m_map.get(cls);
    return xmlClasses == null || xmlClasses.isEmpty()
        ? null
        : xmlClasses.get(xmlClasses.size() - 1);
  }

  /**
   * @return every {@code <class>} tag naming this class, in XML order. Empty for a class no tag
   *     named -- one a {@code @Factory} produced, say.
   */
  public List<XmlClass> getXmlClasses(Class<?> cls) {
    // A copy, mutable whichever branch answered: the occurrences are this map's own state.
    return new ArrayList<>(m_map.getOrDefault(cls, Collections.emptyList()));
  }

  public Set<Class<?>> getClasses() {
    return m_map.keySet();
  }

  public boolean isEmpty() {
    return m_map.isEmpty();
  }
}
