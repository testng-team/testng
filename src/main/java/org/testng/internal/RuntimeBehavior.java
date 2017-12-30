package org.testng.internal;

/**
 * This class houses handling all JVM arguments by TestNG
 */
public final class RuntimeBehavior {

    private RuntimeBehavior() {
    }

    /**
     * @return - returns <code>true</code> if we would like to run in the Dry mode and
     * <code>false</code> otherwise.
     */
    public static boolean isDryRun() {
        String value = System.getProperty("testng.mode.dryrun", "false");
        return Boolean.parseBoolean(value);
    }
}
