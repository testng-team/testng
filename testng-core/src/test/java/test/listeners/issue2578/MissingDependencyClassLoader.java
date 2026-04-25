package test.listeners.issue2578;

import java.io.IOException;
import java.io.InputStream;

public class MissingDependencyClassLoader extends ClassLoader {

  private static final String SAMPLE_PACKAGE = "test.listeners.issue2578.";
  private static final String MISSING_DEPENDENCY = SAMPLE_PACKAGE + "MissingConstructorDependency";

  public MissingDependencyClassLoader() {
    super(MissingDependencyClassLoader.class.getClassLoader());
  }

  public Class<?> loadClass(Class<?> clazz) throws ClassNotFoundException {
    return loadClass(clazz.getName());
  }

  @Override
  protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    if (MISSING_DEPENDENCY.equals(name)) {
      throw new ClassNotFoundException(name);
    }
    if (name.startsWith(SAMPLE_PACKAGE)) {
      Class<?> loadedClass = findLoadedClass(name);
      if (loadedClass == null) {
        loadedClass = findClass(name);
      }
      if (resolve) {
        resolveClass(loadedClass);
      }
      return loadedClass;
    }
    return super.loadClass(name, resolve);
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    String resourceName = name.replace('.', '/') + ".class";
    try (InputStream inputStream = getParent().getResourceAsStream(resourceName)) {
      if (inputStream == null) {
        throw new ClassNotFoundException(name);
      }
      byte[] bytes = inputStream.readAllBytes();
      return defineClass(name, bytes, 0, bytes.length);
    } catch (IOException e) {
      throw new ClassNotFoundException(name, e);
    }
  }
}
