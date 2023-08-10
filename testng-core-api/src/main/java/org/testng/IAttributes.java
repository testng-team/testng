package org.testng;

import java.util.Set;

/** A trait that is used by all interfaces that lets the user add or remove their own attributes. */
public interface IAttributes {
  /**
   * Returns the attribute with the given name.
   *
   * @param name The name of the attribute to return
   */
  Object getAttribute(String name);

  /**
   * Set a custom attribute.
   *
   * @param name The attribute name
   * @param value The attribute value
   */
  void setAttribute(String name, Object value);

  /** Returns all the attributes names. */
  Set<String> getAttributeNames();

  /**
   * Remove the attribute
   *
   * @param name The attribute name
   * @return the attribute value if found, null otherwise
   */
  Object removeAttribute(String name);
}
