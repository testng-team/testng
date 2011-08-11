package org.testng.annotations;

/**
 * Parent interface for annotations that can receive parameters.
 *
 * Created on Dec 20, 2005
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public interface IParameterizable extends IAnnotation {
  /**
   * The list of variables used to fill the parameters of this method.
   * These variables must be defined in the property file.
   *
   * @deprecated Use @Parameters
   */
  @Deprecated
  public String[] getParameters();

  /**
   * Whether this annotation is enabled.
   */
  public boolean getEnabled();
  public void setEnabled(boolean enabled);
}
