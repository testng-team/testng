package org.testng.annotations;

/**
 * Encapsulate the @DataProvider / @testng.data-provider annotation
 * 
 * Created on Dec 20, 2005
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public interface IDataProvider extends IAnnotation {
  /**
   * The name of this DataProvider.
   */
  public String getName();
  public void setName(String name);
}
