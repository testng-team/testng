package org.testng.internal;

public class Version {

    public static final String VERSION = "$VERSION";

    public static String getVersionString() {
        return VERSION;
    }

    public static void displayBanner() {
        System.out.println("...\\n... TestNG " + getVersionString() + " by Cédric Beust (cedric@beust.com)\\n...\\n");
    }
}
