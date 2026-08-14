package org.testng;

import java.util.Optional;

/** Represents a factory method */
public interface IFactoryMethod {

  /**
   * @return - Returns parameters associated with a factory method wrapped within a {@link Optional}
   */
  Optional<Object[]> getParameters();
}
