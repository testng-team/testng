package org.testng;

import java.util.Set;
import org.jspecify.annotations.Nullable;

/** A trait that is used by all interfaces that lets the user add or remove their own attributes. */
public interface IAttributes {
  /**
   * @param name The name of the attribute to return
   * @return The attribute, or {@code null} when no attribute is registered under that name.
   */
  @Nullable
  Object getAttribute(String name);

  /**
   * Set a custom attribute.
   *
   * @param name The attribute name
   * @param value The attribute value
   */
  void setAttribute(String name, Object value);

  /** @return all the attributes names. */
  Set<String> getAttributeNames();

  /**
   * Remove the attribute
   *
   * @param name The attribute name
   * @return the attribute value if found, null otherwise
   */
  @Nullable
  Object removeAttribute(String name);
}
