package org.testng.internal;

import java.util.UUID;

public interface IInstanceIdentity {

  /**
   * @return - A <code>{@link UUID}</code> that represents a unique id which is associated with
   *     every test class object.
   */
  UUID getInstanceId();

  static Object getInstanceId(Object object) {
    if (object instanceof IInstanceIdentity) {
      return ((IInstanceIdentity) object).getInstanceId();
    }
    return object;
  }
}
