package org.testng.internal;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public interface IInstanceIdentity {

  /**
   * @return - A <code>{@link UUID}</code> that represents a unique id which is associated with
   *     every test class object, or {@code null} when the implementation carries no instance.
   */
  @Nullable
  UUID getInstanceId();

  static @Nullable Object getInstanceId(Object object) {
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
