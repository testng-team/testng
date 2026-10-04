package org.testng;

import java.util.List;
import org.testng.internal.MethodInstance;

/**
 * A method interceptor that keeps the order of the {@code <test>} tag.
 *
 * <p>It sorts the classes in the order of their {@code <class>} tags. Inside one class, it sorts
 * the methods in the order of their {@code <include>} tags. Classes that a {@code @Factory} made
 * have no {@code <class>} tag, so they come last. {@link TestRunner} uses this interceptor when
 * {@code preserve-order} is on.
 *
 * @author cbeust
 */
class PreserveOrderMethodInterceptor implements IMethodInterceptor {

  @Override
  public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
    methods.sort(MethodInstance.SORT_BY_INDEX);
    return methods;
  }
}
