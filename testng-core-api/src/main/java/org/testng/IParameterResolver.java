package org.testng;

import java.lang.reflect.Parameter;
import org.jspecify.annotations.Nullable;

/**
 * Lets a third party own a test method parameter that TestNG itself knows nothing about.
 *
 * <p>Implementations of this TestNG listener can be wired in via the <code>&#64;Listeners</code>
 * annotation, via the <code>listeners</code> tag in the suite file, programmatically through <code>
 * TestNG.addListener</code>, or via a Service Provider Interface mechanism.
 *
 * <p>A parameter a resolver claims is <em>removed from data provider matching</em>: the data
 * provider row supplies values for the remaining parameters only, and the resolved value is placed
 * back at its declared position just before the test method is invoked. The same resolution applies
 * to a test method that has no data provider at all.
 *
 * <h2>Without a data provider</h2>
 *
 * <pre>
 * public final class MyResolver implements IParameterResolver {
 *
 *   &#64;Override
 *   public boolean supportsParameter(Parameter parameter, ITestNGMethod method,
 *       ITestContext context) {
 *     return parameter.isAnnotationPresent(MyAnnotation.class);
 *   }
 *
 *   &#64;Override
 *   public Object resolveParameter(Parameter parameter, ITestNGMethod method,
 *       ITestContext context) {
 *     return new MyObject();
 *   }
 * }
 * </pre>
 *
 * <pre>
 * &#64;Test
 * void example(&#64;MyAnnotation MyObject object) {
 *   // object was supplied by MyResolver
 * }
 * </pre>
 *
 * <h2>Coexisting with a data provider</h2>
 *
 * <pre>
 * &#64;DataProvider(name = "dp")
 * public Object[][] dp() {
 *   return new Object[][] {{"value", 42}};
 * }
 *
 * &#64;Test(dataProvider = "dp")
 * void example(String a, &#64;MyAnnotation MyObject object, int b) {
 *   // a == "value", object came from MyResolver, b == 42
 * }
 * </pre>
 *
 * <h2>Contract</h2>
 *
 * <ul>
 *   <li><b>Native injection wins.</b> A resolver is never asked about a parameter TestNG injects
 *       natively -- {@link java.lang.reflect.Method}, {@link ITestContext}, {@link ITestResult} and
 *       {@link org.testng.xml.XmlTest} -- so it cannot displace one. A parameter carrying {@link
 *       org.testng.annotations.NoInjection} is not natively injected, and is therefore offered to
 *       the resolvers before falling through to the data provider.
 *   <li><b>Ownership is exclusive.</b> If more than one enabled resolver claims the same parameter,
 *       TestNG fails the method with a {@link TestNGException} naming the test method, the
 *       parameter and every competing resolver, rather than picking one by registration order.
 *   <li><b>A parameter no resolver claims is untouched.</b> It keeps going through the existing
 *       TestNG and data provider matching rules, with the existing diagnostics.
 *   <li><b>Failures are not swallowed.</b> An exception thrown by either method is reported as a
 *       {@link TestNGException} that keeps the original as its cause and names the resolver, the
 *       test method and the parameter.
 *   <li><b>The resolved value is checked.</b> It must be assignable to the parameter type, with the
 *       usual boxing and widening; {@code null} is accepted for a reference type and rejected for a
 *       primitive one. An incompatible value is reported as a TestNG diagnostic rather than as an
 *       {@code IllegalArgumentException} out of the reflective call.
 * </ul>
 *
 * <h2>Lifecycle</h2>
 *
 * <p>{@link #supportsParameter} is consulted while the arguments of an invocation are being built,
 * and must be a stable, side effect free decision for a given parameter. {@link #resolveParameter}
 * is called once per parameter per invocation -- so once per data provider row -- immediately
 * before the test method runs, which is where a resolver creates whatever per invocation state it
 * owns.
 *
 * <p>A resolver registered on a suite is visible to every {@code <test>} of that suite, which is
 * the scope the other data driven listeners already have.
 *
 * <p>This applies to {@code &#64;Test} methods only. Constructors, {@code &#64;Factory} methods and
 * configuration methods are not resolved.
 *
 * @since 7.13.0
 */
public interface IParameterResolver extends ITestNGListener {

  /**
   * Decides whether this resolver owns the given parameter.
   *
   * @param parameter - The {@link Parameter} being matched. It is never one TestNG injects
   *     natively.
   * @param method - The {@link ITestNGMethod} that declares it.
   * @param context - The {@link ITestContext} the method belongs to.
   * @return - <code>true</code> if this resolver supplies the value of that parameter, in which
   *     case the parameter is excluded from data provider matching.
   */
  boolean supportsParameter(Parameter parameter, ITestNGMethod method, ITestContext context);

  /**
   * Supplies the value of a parameter this resolver {@linkplain #supportsParameter claimed}, once
   * per invocation.
   *
   * @param parameter - The {@link Parameter} to supply a value for.
   * @param method - The {@link ITestNGMethod} that declares it.
   * @param context - The {@link ITestContext} the method belongs to.
   * @return - The value to invoke the test method with. May be <code>null</code> for a reference
   *     typed parameter.
   */
  @Nullable
  Object resolveParameter(Parameter parameter, ITestNGMethod method, ITestContext context);
}
