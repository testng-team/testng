package org.testng;

import java.util.List;

/**
 * This class is used to alter the list of test methods that TestNG is about to run.
 *
 * <p>An instance of this class will be invoked right before TestNG starts invoking test methods.
 * Every test method of the {@code <test>} is passed in parameter, the ones taking part in a
 * dependency included: the graph the run is scheduled on is built from what an interceptor returns,
 * so a method withheld here would be one no implementation could ever drop. Implementers of this
 * interface need to return a list of {@link IMethodInstance} that represents the list of test
 * methods they want run. TestNG will run these methods in the same order found in the returned
 * value.
 *
 * <p>What each method is bound to is readable from {@link ITestNGMethod#upstreamDependencies()} and
 * {@link ITestNGMethod#downstreamDependencies()}, both populated by the time an interceptor
 * registered by the user is invoked. They answer every ordering constraint the scheduling graph
 * holds for the method, which is more than what its annotations declare: the order TestNG derives
 * on its own from {@code preserve-order} and {@code group-by-instances} is in there too. In a
 * default sequential {@code <test>} listing two classes, every method of the second reports every
 * method of the first as an upstream, without a single {@code dependsOnMethods} in the suite.
 *
 * <p>Where a method runs stays bounded by that graph, so reordering one in the returned list cannot
 * move it ahead of what it is bound to. Dropping one does take effect -- that is what makes the
 * whole set worth passing -- and is safe for a constraint TestNG derived itself. Dropping a method
 * that another <em>retained</em> method <em>declares</em> a dependency upon is what ends the run,
 * with a {@link TestNGException}: the {@code dependsOnMethods} then names a method that is no
 * longer part of the run.
 *
 * <p>Typically, the returned list will be just the methods passed in parameter but sorted
 * differently, but it can actually have any size (it can be empty, it can be of the same size as
 * the original list or it can contain more methods).
 *
 * <p>The {@link ITestContext} is passed in the <code>intercept</code> method so that implementers
 * can set user values (using {@link ITestContext#setAttribute(String, Object)}), which they can
 * then look up later while generating the reports.
 *
 * @author cbeust
 */
public interface IMethodInterceptor extends ITestNGListener {

  List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context);
}
