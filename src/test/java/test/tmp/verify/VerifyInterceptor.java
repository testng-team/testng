package test.tmp.verify;

import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.TestNGUtils;
import org.testng.collections.Maps;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VerifyInterceptor implements IMethodInterceptor {

  /**
   * @return the list of methods received in parameters with all methods
   * annotated with @Verify inserted after each of these test methods.
   *
   * This happens in two steps:
   * - Find all the methods annotated with @Verify in the classes that contain test methods
   * - Insert these verify methods after each method passed in parameter
   * These @Verify methods are stored in a map keyed by the class in order to avoid looking them
   * up more than once on the same class.
   */
  @Override
  public List<IMethodInstance> intercept(List<IMethodInstance> methods,
      ITestContext context) {

    List<IMethodInstance> result = new ArrayList<>();
    Map<Class<?>, List<IMethodInstance>> verifyMethods = Maps.newHashMap();
    for (IMethodInstance mi : methods) {
      ITestNGMethod tm = mi.getMethod();
      List<IMethodInstance> verify = verifyMethods.get(tm.getRealClass());
      if (verify == null) {
        verify = findVerifyMethods(tm.getRealClass(), tm);
      }
      result.add(mi);
      result.addAll(verify);
    }

    return result;
  }

  /**
   * @return all the @Verify methods found on @code{realClass}
   */
  private List<IMethodInstance> findVerifyMethods(Class realClass, final ITestNGMethod tm) {
    List<IMethodInstance> result = new ArrayList<>();
    for (final Method m : realClass.getDeclaredMethods()) {
      Annotation a = m.getAnnotation(Verify.class);
      if (a != null) {
        final ITestNGMethod vm = TestNGUtils.createITestNGMethod(tm, m);
        result.add(new IMethodInstance() {

          @Override
          public Object[] getInstances() {
            return tm.getInstances();
          }

          @Override
          public ITestNGMethod getMethod() {
            return vm;
          }

          public int compareTo(IMethodInstance o) {
            if (getInstances()[0] == o.getInstances()[0]) {
              return 0;
            } else {
              return -1;
            }
          }

          @Override
          public Object getInstance() {
            return tm.getInstance();
          }
        });
      }
    }

    return result;
  }
}
