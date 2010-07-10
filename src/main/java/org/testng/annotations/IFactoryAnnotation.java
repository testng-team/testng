package org.testng.annotations;

/**
 * Encapsulate the @Factory / @testng.factory annotation
 * 
 * Created on Dec 20, 2005
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public interface IFactoryAnnotation extends IParameterizable {
  public String getDataProvider();
  public void setDataProvider(String dataProvider);
}
