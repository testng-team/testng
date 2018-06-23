package org.testng;

import java.util.Set;

/** A trait that is used by all interfaces that lets the user add or remove their own attributes. */
public interface IAttributes {
  /** @param name The name of the attribute to return */
  Object getAttribute(String name);

  /** Set a custom attribute. */
  void setAttribute(String name, Object value);

  /** @return all the attributes names. */
  Set<String> getAttributeNames();

  /**
   * Remove the attribute
   *
   * @return the attribute value if found, null otherwise
   */
  Object removeAttribute(String name);
}
