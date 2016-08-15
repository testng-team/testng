package org.testng.internal;

import org.testng.ITestNGMethod;

import java.util.List;

/**
 * Collections of helper methods to help deal with TestNG configuration methods
 */
class TestNgMethodUtils {

    private TestNgMethodUtils() {
        //Utility class. So hiding the constructor.
    }

    /**
     * A helper method that checks to see if a method is a configuration method or not.
     * @param method - A {@link ITestNGMethod} object which needs to be checked.
     * @return - <code>true</code> if the method is a configuration method and false if its a test method.
     */
    static boolean isConfigurationMethod(ITestNGMethod method) {
        return isConfigurationMethod(method, false);
    }

    /**
     *
     * A helper method that checks to see if a method is a configuration method or not.
     * @param method - A {@link ITestNGMethod} object which needs to be checked.
     * @param includeGroupConfigs - <code>true</code> if before/after group configuration annotations are also to
     *                            be taken into consideration.
     * @return - <code>true</code> if the method is a configuration method and false if its a test method.
     */
    private static boolean isConfigurationMethod(ITestNGMethod method, boolean includeGroupConfigs) {
        boolean flag =  method.isBeforeMethodConfiguration() || method.isAfterMethodConfiguration() ||
            method.isBeforeTestConfiguration()  || method.isAfterTestConfiguration() ||
            method.isBeforeClassConfiguration() || method.isAfterClassConfiguration() ||
            method.isBeforeSuiteConfiguration() || method.isAfterSuiteConfiguration();
        if (includeGroupConfigs) {
            flag = flag || method.isBeforeGroupsConfiguration() || method.isAfterGroupsConfiguration();
        }
        return flag;
    }

    /**
     * A helper method which checks if a given method is a configuration method and is part of list of TestNG methods
     * @param method - A {@link ITestNGMethod} object which needs to be checked.
     * @param methods - A List of {@link ITestNGMethod} in which the check needs to be done.
     * @return - <code>true</code> if the method is a configuration method and exists in the list of methods passed.
     */
    static boolean containsConfigurationMethod(ITestNGMethod method, List<ITestNGMethod> methods) {
        return isConfigurationMethod(method, true) && methods.contains(method);
    }
}
