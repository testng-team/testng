package org.testng.internal;

import java.util.List;
import org.testng.ITestNGMethod;
import org.testng.collections.Lists;

public interface ITestClassConfigInfo {

  /**
   * get all before class config methods
   *
   * @return all before class config methods
   */
  default List<ITestNGMethod> getAllBeforeClassMethods() {
    return Lists.newArrayList();
  }

  /**
   * Query the instance before class methods from config methods map.
   *
   * @param instance object hashcode
   * @return All before class methods of instance
   */
  default List<ITestNGMethod> getInstanceBeforeClassMethods(Object instance) {
    return Lists.newArrayList();
  }
}
