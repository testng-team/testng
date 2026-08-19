package org.testng.internal;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

public interface IInstanceIdentity {

  /**
   * The token {@link #getInstanceId(Object)} answers for a method that carries no instance.
   *
   * <p>Grouping by instance keys on the answer, and a map key that may be absent forces every such
   * map to accept a null key and every caller to decide what an absent one means. One shared token
   * keeps the key present and the grouping identical: every method without an instance lands in the
   * same bucket, which is what a null key did.
   */
  Object NO_INSTANCE =
      new Object() {
        @Override
        public String toString() {
          return "NO_INSTANCE";
        }
      };

  /**
   * @return - A <code>{@link UUID}</code> that represents a unique id which is associated with
   *     every test class object, or {@code null} when the implementation carries no instance.
   */
  @Nullable
  UUID getInstanceId();

  /**
   * @param object - The object to read an instance id from.
   * @return - The object's instance id when it is identity aware, {@link #NO_INSTANCE} when it is
   *     identity aware but carries no instance, and the object itself otherwise.
   */
  /**
   * @param objects - The objects to inspect
   * @return - <code>true</code> if all the objects passed are of type {@link IInstanceIdentity}
   */
  static boolean isIdentityAware(Object... objects) {
    return java.util.Arrays.stream(java.util.Objects.requireNonNull(objects))
        .allMatch(it -> it instanceof IInstanceIdentity);
  }

  static Object getInstanceId(Object object) {
    if (object instanceof IInstanceIdentity) {
      UUID instanceId = ((IInstanceIdentity) object).getInstanceId();
      return instanceId == null ? NO_INSTANCE : instanceId;
    }
    return object;
  }
}
