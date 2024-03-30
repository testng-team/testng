package org.testng.internal;

import java.util.Arrays;
import java.util.Objects;
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

  /**
   * @param objects - The objects to inspect
   * @return - <code>true</code> if all the objects passed are of type {@link IInstanceIdentity}
   */
  static boolean isIdentityAware(Object... objects) {
    return Arrays.stream(Objects.requireNonNull(objects))
        .allMatch(it -> it instanceof IInstanceIdentity);
  }
}
