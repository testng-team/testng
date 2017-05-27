package org.testng.internal;

import org.testng.IConfigurationListener;
import org.testng.ITestListener;


/**
 * A convenient interface to use when implementing listeners.
 * @author <a href='mailto:the_mindstorm@evolva.ro'>Alexandru Popescu</a>
 */
public interface IResultListener extends ITestListener, IConfigurationListener {

}
