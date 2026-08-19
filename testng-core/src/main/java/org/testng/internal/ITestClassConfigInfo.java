package org.testng.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.testng.ITestClass;
import org.testng.ITestNGMethod;
import org.jspecify.annotations.Nullable;

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
   * @param instanceId the per-instance id (UUID) of the test class instance
   * @return All before class methods of instance
   */
  List<ITestNGMethod> getInstanceBeforeClassMethods(@Nullable UUID instanceId);

  List<ITestNGMethod> getInstanceAfterClassMethods(@Nullable UUID instanceId);

  static List<ITestNGMethod> allBeforeClassMethods(ITestClass tc) {
    if (tc instanceof ITestClassConfigInfo) {
      return ((ITestClassConfigInfo) tc).getAllBeforeClassMethods();
    }
    return new ArrayList<>();
  }

  static List<ITestNGMethod> allAfterClassMethods(ITestClass tc) {
    if (tc instanceof ITestClassConfigInfo) {
      return ((ITestClassConfigInfo) tc).getAllAfterClassMethods();
    }
    return new ArrayList<>();
  }
}
