package org.testng;

import java.util.List;

/**
 * This class is used to alter the list of test methods that TestNG is about to run.
 *
 * <p>
 *
 * An instance of this class will be invoked right before TestNG starts invoking test methods.
 * Only methods that have no dependents and that don't depend on any other test methods will
 * be passed in parameter.  Implementers of this interface need to return a list of {@link IMethodInstance}
 * that represents the list of test methods they want run.  TestNG will run these methods in the
 * same order found in the returned value.
 *
 * <p>
 *
 * Typically, the returned list will be just the methods passed in parameter but sorted
 * differently, but it can actually have any size (it can be empty, it can be of the
 * same size as the original list or it can contain more methods).
 *
 * <p>
 *
 * The {@link ITestContext} is passed in the <tt>intercept</tt> method so that implementers can set user values
 * (using {@link ITestContext#setAttribute(String, Object)}), which they can then look up
 * later while generating the reports.
 *
 * @author cbeust
 */
public interface IMethodInterceptor extends ITestNGListener {

  List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context);

}
