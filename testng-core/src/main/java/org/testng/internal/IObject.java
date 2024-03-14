package org.testng.internal;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents the associations of a class with one or more instances. Relevant with <code>@Factory
 * </code> annotation.
 */
public interface IObject {

  /**
   * Returns all the instances the methods will be invoked upon. This will typically be an array of
   * one object in the absence of a @Factory annotation.
   *
   * @param create - <code>true</code> if objects should be created before returning.
   * @param errorMsgPrefix - Text that should be prefixed to the error message when there are
   *     issues. Can be empty.
   * @return - An array of {@link IdentifiableObject} objects
   */
  IdentifiableObject[] getObjects(boolean create, String errorMsgPrefix);

  /** @return - An array representing the hash codes of the corresponding instances. */
  long[] getInstanceHashCodes();

  /** @param instance - The instance that should be added to the list of instances. */
  void addObject(IdentifiableObject instance);

  /**
   * @param object - The object that should be inspected for its compatibility with {@link IObject}.
   * @return - An array representing the hash codes of the corresponding instances.
   */
  static long[] instanceHashCodes(Object object) {
    return cast(object).map(IObject::getInstanceHashCodes).orElse(new long[] {});
  }

  /**
   * @param object - The object that should be inspected for its compatibility with {@link IObject}.
   * @param create - <code>true</code> if objects should be created before returning.
   * @return - An array (can be empty is instance compatibility fails) of {@link IdentifiableObject}
   *     objects.
   */
  static IdentifiableObject[] objects(Object object, boolean create) {
    return objects(object, create, "");
  }

  /**
   * @param object - The object that should be inspected for its compatibility with {@link IObject}.
   * @param create - <code>true</code> if objects should be created before returning.
   * @param errorMsgPrefix - Text that should be prefixed to the error message when there are
   *     issues. Can be empty.
   * @return - An array (can be empty is instance compatibility fails) of {@link IdentifiableObject}
   *     objects.
   */
  static IdentifiableObject[] objects(Object object, boolean create, String errorMsgPrefix) {
    return cast(object)
        .map(it -> it.getObjects(create, errorMsgPrefix))
        .orElse(new IdentifiableObject[] {});
  }

  /**
   * @param object - The object that should be inspected for its compatibility with {@link IObject}.
   * @return - If the incoming object is an instance of {@link IObject} then the cast instance is
   *     wrapped within {@link Optional} else it would be an {@link Optional#empty()}
   */
  static Optional<IObject> cast(Object object) {
    if (object instanceof IObject) {
      return Optional.of((IObject) object);
    }
    return Optional.empty();
  }

  /** A wrapper object that associates a unique id to every unique test class object. */
  class IdentifiableObject {
    private final Object instance;
    private final UUID instanceId;

    public IdentifiableObject(Object instance) {
      this(instance, UUID.randomUUID());
    }

    public IdentifiableObject(Object instance, UUID instanceId) {
      this.instance = instance;
      this.instanceId = instanceId;
    }

    public static Object unwrap(IdentifiableObject object) {
      if (object == null) {
        return null;
      }
      return object.getInstance();
    }

    public UUID getInstanceId() {
      return instanceId;
    }

    public Object getInstance() {
      return instance;
    }

    @Override
    public boolean equals(Object object) {
      if (this == object) return true;
      if (object == null || getClass() != object.getClass()) return false;
      IdentifiableObject that = (IdentifiableObject) object;
      return Objects.equals(instanceId, that.instanceId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(instanceId);
    }
  }
}
