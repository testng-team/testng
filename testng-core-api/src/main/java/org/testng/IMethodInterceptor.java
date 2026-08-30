package org.testng;

import java.util.List;

/**
 * This class is used to alter the list of test methods that TestNG is about to run.
 *
 * <p>An instance of this class will be invoked right before TestNG starts invoking test methods. It
 * is given every test method of the {@code <test>}, including the ones that declare a dependency
 * and the ones another method depends upon. Implementers of this interface need to return a list of
 * {@link IMethodInstance} that represents the list of test methods they want run. TestNG will run
 * these methods in the same order found in the returned value.
 *
 * <p>That order is a preference, not a licence: a method that has to run after another one still
 * does, wherever it is placed in the returned list. {@link ITestNGMethod#upstreamDependencies()}
 * and {@link ITestNGMethod#downstreamDependencies()} name what a method has to run after and what
 * has to run after it, and are filled in before this method is called. They cover the {@code
 * dependsOnMethods} and {@code dependsOnGroups} a method takes part in, and nothing else: the order
 * {@code preserve-order} or {@code group-by-instances} imposes is TestNG's own, and dropping or
 * moving a method it covers costs nothing.
 *
 * <p>Leaving a method out of the returned list does keep it from running, with one exception: if
 * another method that is kept declares a {@code dependsOnMethods} or {@code dependsOnGroups} on it,
 * the run ends with a {@link TestNGException}.
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
