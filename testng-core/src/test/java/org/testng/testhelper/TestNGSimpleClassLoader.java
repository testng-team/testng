package org.testng.testhelper;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public final class TestNGSimpleClassLoader extends ClassLoader {

  private final File baseDir;

  public TestNGSimpleClassLoader() {
    this(null);
  }

  public TestNGSimpleClassLoader(File baseDir) {
    this.baseDir = baseDir;
  }

  public Class<?> injectByteCode(CompiledCode byteCode) throws ClassNotFoundException {
    Class<?> clazz =
        defineClass(byteCode.getName(), byteCode.getByteCode(), 0, byteCode.getByteCode().length);
    return loadClass(clazz.getName());
  }

  @Override
  protected URL findResource(String name) {
    if (this.baseDir != null) {
      try {
        return new File(this.baseDir.getAbsolutePath() + "/" + name).toURI().toURL();
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
    }
    return super.findResource(name);
  }
}
