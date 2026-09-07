package test.reflect;

/**
 * A deliberately dependency-free class used by {@link ExecutableCacheClassLoaderTest}. It is loaded
 * through a throwaway class loader so the test can prove that interning one of its methods does not
 * keep that loader alive. It must not reference any other test class, otherwise the throwaway
 * loader would drag more classes in.
 */
public class LoneClass {
  public void ping() {}
}
