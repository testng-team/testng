package test;

import java.security.Permission;

public class MySecurityManager extends SecurityManager {

  private SecurityManager baseSecurityManager;

  public MySecurityManager(SecurityManager baseSecurityManager) {
    this.baseSecurityManager = baseSecurityManager;
  }

  @Override
  public void checkPermission(Permission permission) {
    if (permission.getName().startsWith("exitVM")) {
      throw new SecurityException("System exit not allowed");
    }
    if (baseSecurityManager != null) {
      baseSecurityManager.checkPermission(permission);
    }
  }
}
