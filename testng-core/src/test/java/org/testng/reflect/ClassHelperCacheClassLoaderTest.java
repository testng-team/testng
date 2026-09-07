package test.reflect;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.Set;
import org.testng.annotations.Test;
import org.testng.internal.ClassHelper;

/**
 * Proves the main reason for memoizing {@link ClassHelper#getAvailableMethods(Class)} on {@link
 * ClassValue}: looking a class's methods up does not keep that class (or its class loader) alive.
 * The per-class table lives inside the class itself, so once the class is unreachable the whole
 * thing can be collected.
 */
public class ClassHelperCacheClassLoaderTest {

  @Test
  public void lookingUpAvailableMethodsDoesNotPinTheClassLoader() throws Exception {
    WeakReference<ClassLoader> loaderRef = lookupThroughThrowawayLoader();

    assertThat(collect(loaderRef))
        .as("the throwaway class loader must be collectible after a cached method lookup")
        .isNull();
  }

  /**
   * Loads {@link LoneClass} through a throwaway loader, asks {@link ClassHelper} for its available
   * methods, and returns a weak reference to that loader. When this method returns, the loader and
   * its class are reachable only through the class's own {@link ClassValue} slot.
   */
  private static WeakReference<ClassLoader> lookupThroughThrowawayLoader() throws Exception {
    ClassLoader loader = new SingleClassLoader(LoneClass.class.getName());
    Class<?> loaded = loader.loadClass(LoneClass.class.getName());
    assertThat(loaded.getClassLoader())
        .as("the class must belong to the throwaway loader, not the app loader")
        .isSameAs(loader);

    Set<Method> methods = ClassHelper.getAvailableMethods(loaded);
    assertThat(methods).isNotEmpty();
    assertThat(methods.iterator().next().getDeclaringClass()).isSameAs(loaded);

    return new WeakReference<>(loader);
  }

  private static ClassLoader collect(WeakReference<ClassLoader> ref) {
    for (int i = 0; i < 50 && ref.get() != null; i++) {
      System.gc();
      try {
        Thread.sleep(50);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
    return ref.get();
  }

  /**
   * A loader that defines exactly one named class itself (reading the bytes from its parent) and
   * delegates everything else upward. Defining the class here makes this loader its owner, so the
   * class belongs to this loader and dies with it.
   */
  private static final class SingleClassLoader extends ClassLoader {
    private final String owned;

    SingleClassLoader(String owned) {
      super(SingleClassLoader.class.getClassLoader());
      this.owned = owned;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
      if (owned.equals(name)) {
        Class<?> loaded = findLoadedClass(name);
        if (loaded == null) {
          loaded = findClass(name);
        }
        if (resolve) {
          resolveClass(loaded);
        }
        return loaded;
      }
      return super.loadClass(name, resolve);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
      String resource = name.replace('.', '/') + ".class";
      try (InputStream in = getParent().getResourceAsStream(resource)) {
        if (in == null) {
          throw new ClassNotFoundException(name);
        }
        byte[] bytes = in.readAllBytes();
        return defineClass(name, bytes, 0, bytes.length);
      } catch (IOException e) {
        throw new ClassNotFoundException(name, e);
      }
    }
  }
}
