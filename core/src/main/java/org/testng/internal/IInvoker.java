package org.testng.internal;

/**
 * This class defines an invoker.
 *
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public interface IInvoker {

  ITestInvoker getTestInvoker();

  IConfigInvoker getConfigInvoker();
}
