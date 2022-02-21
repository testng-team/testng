package org.testng.internal;

import java.io.IOException;
import java.io.InputStream;
import org.testng.log4testng.Logger;

public class DataProviderLoader extends ClassLoader {
  private static final int BUFFER_SIZE = 1 << 20;
  private static final Logger log = Logger.getLogger(DataProviderLoader.class);

  public Class loadClazz(String path) throws ClassNotFoundException {
    Class clazz = findLoadedClass(path);
    if (clazz == null) {
      byte[] bt = loadClassData(path);
      clazz = defineClass(path, bt, 0, bt.length);
    }

    return clazz;
  }

  private byte[] loadClassData(String className) throws ClassNotFoundException {
    InputStream in =
        this.getClass()
            .getClassLoader()
            .getResourceAsStream(className.replace(".", "/") + ".class");
    if (in == null) {
      throw new ClassNotFoundException("Cannot load resource input stream: " + className);
    }

    byte[] classBytes;
    try {
      classBytes = in.readAllBytes();
    } catch (IOException e) {
      throw new ClassNotFoundException("ERROR reading class file" + e);
    }

    if (classBytes == null) {
      throw new ClassNotFoundException("Cannot load class: " + className);
    }

    return classBytes;
  }
}
