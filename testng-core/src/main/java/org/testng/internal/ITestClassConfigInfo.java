package org.testng.internal;

import java.util.List;
import org.testng.ITestClass;
import org.testng.ITestNGMethod;
import org.testng.collections.Lists;

public interface ITestClassConfigInfo {

  /**
   * get all before class config methods
   *
   * @return all before class config methods
   */
  List<ITestNGMethod> getAllBeforeClassMethods();

  List<ITestNGMethod> getAllAfterClassMethods();

  /**
   * Query the instance before class methods from config methods map.
   *
   * @param instance object hashcode
   * @return All before class methods of instance
   */
  List<ITestNGMethod> getInstanceBeforeClassMethods(Object instance);

  List<ITestNGMethod> getInstanceAfterClassMethods(Object instance);

  static List<ITestNGMethod> allBeforeClassMethods(ITestClass tc) {
    if (tc instanceof ITestClassConfigInfo) {
      return ((ITestClassConfigInfo) tc).getAllBeforeClassMethods();
    }
    return Lists.newArrayList();
  }

  static List<ITestNGMethod> allAfterClassMethods(ITestClass tc) {
    if (tc instanceof ITestClassConfigInfo) {
      return ((ITestClassConfigInfo) tc).getAllAfterClassMethods();
    }
    return Lists.newArrayList();
  }
}
