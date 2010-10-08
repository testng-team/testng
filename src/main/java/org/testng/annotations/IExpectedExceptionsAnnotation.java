package org.testng.annotations;

/**
 * Encapsulate the @ExpectedExceptions / @testng.expected-exceptions annotation
 * Created on Dec 20, 2005
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public interface IExpectedExceptionsAnnotation extends IAnnotation {
  /**
   * The list of exceptions expected to be thrown by this method.
   */
  public Class[] getValue();

}
