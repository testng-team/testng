package org.testng.internal.annotations;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * The parent interface for all the annotations.
 * 
 * Created on Dec 20, 2005
 * 
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public interface IAnnotation {

  public Method getMethod();
  public void setMethod(Method m);

  public Constructor getConstructor();
  public void setConstructor(Constructor m);

  public Class getTestClass();
  public void setTestClass(Class cls);

}
