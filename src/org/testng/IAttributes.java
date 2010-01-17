package org.testng;

import java.io.Serializable;

public interface IAttributes extends Serializable {
  /**
   * @param name The name of the attribute to return
   */
  public Object getAttribute(String name);

  /**
   * Set a custom attribute.
   */
  public void setAttribute(String name, Object value);

}
