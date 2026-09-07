package test.github3234;

import java.io.IOException;
import java.io.InputStream;

/**
 * Defines this package's samples itself and refuses to load {@link MissingType}, so method
 * signatures that name that type fail with {@code NoClassDefFoundError}.
 */
public class MissingTypeClassLoader extends ClassLoader {

  private static final String SAMPLE_PACKAGE = "test.github3234.";
  private static final String MISSING_TYPE = SAMPLE_PACKAGE + "MissingType";

  public MissingTypeClassLoader() {
    super(MissingTypeClassLoader.class.getClassLoader());
  }

  @Override
  protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    if (MISSING_TYPE.equals(name)) {
      throw new ClassNotFoundException(name);
    }
    if (name.startsWith(SAMPLE_PACKAGE) && !name.equals(IssueTest.class.getName())) {
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
