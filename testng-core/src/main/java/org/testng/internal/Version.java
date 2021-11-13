package org.testng.internal;

public final class Version {

  public static final String VERSION = initVersion();

  private static String initVersion() {
    final String version = Version.class.getPackage().getImplementationVersion();
    return version != null ? version : "[WORKING]";
  }

  public static String getVersionString() {
    return VERSION;
  }

  public static void displayBanner() {
    System.out.println(
        "...\n... TestNG " + getVersionString() + " by Cédric Beust (cedric@beust.com)\n...\n");
  }
}
