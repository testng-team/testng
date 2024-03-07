package test.listeners.issue3082;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class ObjectRepository {

  private ObjectRepository() {}

  private static final Map<UUID, List<String>> mapping = new ConcurrentHashMap<>();

  private static final Set<UUID> errorObjects = ConcurrentHashMap.newKeySet();

  public static void add(UUID uuid, String methodName) {
    mapping
        .computeIfAbsent(uuid, k -> Collections.synchronizedList(new ArrayList<>()))
        .add(methodName);
  }

  public static void recordErrorFor(UUID uuid) {
    errorObjects.add(uuid);
  }

  public static Set<UUID> errors() {
    return Collections.unmodifiableSet(errorObjects);
  }

  public static int instancesCount() {
    return mapping.keySet().size();
  }

  public static Collection<List<String>> invocations() {
    return Collections.unmodifiableCollection(mapping.values());
  }

  public static boolean hasMapping(UUID uuid, String methodName) {
    return Optional.ofNullable(mapping.get(uuid)).map(it -> it.contains(methodName)).orElse(false);
  }
}
