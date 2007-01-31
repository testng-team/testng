package org.testng.internal.annotations;

/**
 * Encapsulate the @Factory / @testng.factory annotation
 * 
 * Created on Dec 20, 2005
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public interface IFactory extends IParameterizable {
  public String getDataProvider();
}
