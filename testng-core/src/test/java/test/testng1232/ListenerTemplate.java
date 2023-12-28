package test.testng1232;

import org.testng.*;

/**
 * This class provides "void" implementations for all listener invocations so that one can tweak
 * behavior of only those methods which need customization. (Mainly to circumvent verbosity in
 * actual listener implementations)
 */
public class ListenerTemplate
    implements IInvokedMethodListener,
        IClassListener,
        ITestListener,
        ISuiteListener,
        IAlterSuiteListener,
        IExecutionListener,
        IReporter {}
