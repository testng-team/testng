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

/**
 * Maps each test class of a {@code <test>} to the {@code <class>} tags that name it.
 *
 * <p>A {@code <test>} can name a class in more than one {@code <class>} tag. Each tag has its own
 * parameters and its own {@code <methods>}. The map can also hold the public nested classes of each
 * class, under the tag of the outer class. The map keeps the classes in the order in which they
 * were added.
 */
public class ClassInfoMap {

  // For each class, the <class> tags that name it, in the order of the <test>. A <test> can repeat
  // the tag, and each copy has its own parameters and its own <methods>.
  private final Map<Class<?>, List<XmlClass>> m_map = new LinkedHashMap<>();
  // The classes that a <class> tag names directly. A nested class that the map holds under the tag
  // of its outer class is not in this set.
  private final Set<Class<?>> m_named = new HashSet<>();
  private final boolean includeNestedClasses;

  /** Creates an empty map. */
  public ClassInfoMap() {
    this(Collections.emptyList(), false);
  }

  /**
   * Creates a map of {@code classes}, and of the public nested classes of each class.
   *
   * @param classes the {@code <class>} tags of the {@code <test>}.
   */
  public ClassInfoMap(List<XmlClass> classes) {
    this(classes, true);
  }

  /**
   * Creates a map of {@code classes}.
   *
   * <p>When a class cannot load because a class that it uses is missing, this constructor logs the
   * error. It throws the error only when the {@code <class>} tag must load its class.
   *
   * @param classes the {@code <class>} tags of the {@code <test>}.
   * @param includeNested {@code true} to also map the public nested classes of each class.
   * @throws NoClassDefFoundError when a class cannot load, and its tag must load it.
   */
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
      // The first tag that names this class replaces the tags that an outer class added for it. A
      // nested class with a tag of its own follows that tag, and not the tag of its outer class.
      // Later tags for this class add more copies to the list. Clear the list in place, and do not
      // add the key again, because the order of the keys becomes the order of the test classes.
      occurrences.clear();
    }
    occurrences.add(xmlClass);
    Set<Class<?>> visited = new HashSet<>();
    visited.add(cl);
    registerNestedClassesOf(cl, xmlClass, visited);
  }

  /**
   * Adds the public nested classes of {@code cl} under the tag {@code xmlClass}. A nested class
   * that has a tag of its own keeps that tag.
   *
   * @param visited the classes that this walk has already reached. {@code Class#getClasses} also
   *     returns the inherited member classes. So for {@code class A { class B extends A {} }}, the
   *     walk would go into {@code B} for ever without this set.
   */
  private void registerNestedClassesOf(Class<?> cl, XmlClass xmlClass, Set<Class<?>> visited) {
    if (!includeNestedClasses) {
      return;
    }
    for (Class<?> c : cl.getClasses()) {
      // Check whether a tag names the class, not whether the map holds it. A nested class that the
      // outer tag added belongs to that tag, so it gets every copy of that tag. Only a tag of its
      // own replaces the outer tag.
      if (!m_named.contains(c) && visited.add(c)) {
        m_map.computeIfAbsent(c, key -> new ArrayList<>()).add(xmlClass);
        registerNestedClassesOf(c, xmlClass, visited);
      }
    }
  }

  /**
   * Adds {@code cls} with no {@code <class>} tag, unless the map already holds it.
   *
   * @param cls the class to add.
   */
  public void addClass(Class<?> cls) {
    m_map.computeIfAbsent(cls, key -> new ArrayList<>());
  }

  /**
   * Returns the last {@code <class>} tag for {@code cls}.
   *
   * <p>This method is for a caller that can handle only one tag. {@link #getXmlClasses(Class)}
   * returns all of them.
   *
   * @param cls the class.
   * @return the last tag, or {@code null} when the map has no tag for {@code cls}.
   */
  public @Nullable XmlClass getXmlClass(Class<?> cls) {
    List<XmlClass> xmlClasses = m_map.get(cls);
    return xmlClasses == null || xmlClasses.isEmpty()
        ? null
        : xmlClasses.get(xmlClasses.size() - 1);
  }

  /**
   * Returns all the {@code <class>} tags for {@code cls}, in the order of the XML.
   *
   * @param cls the class.
   * @return a new list of the tags. It is empty for a class that no tag names, for example a class
   *     that a {@code @Factory} created.
   */
  public List<XmlClass> getXmlClasses(Class<?> cls) {
    // Return a new list that the caller can change, also when the map has no list for the class.
    // The lists in the map belong to this map.
    return new ArrayList<>(m_map.getOrDefault(cls, Collections.emptyList()));
  }

  /**
   * Returns the classes in the map, in the order in which they were added.
   *
   * @return a view of the classes, which changes with the map.
   */
  public Set<Class<?>> getClasses() {
    return m_map.keySet();
  }

  /**
   * Tells if the map holds no class.
   *
   * @return {@code true} when the map is empty.
   */
  public boolean isEmpty() {
    return m_map.isEmpty();
  }
}
