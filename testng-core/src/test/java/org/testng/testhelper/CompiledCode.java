package org.testng.testhelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javax.tools.JavaFileObject.Kind;

public class CompiledCode {
  private final byte[] byteCode;
  private final boolean skipLoading;
  private final String name;

  public CompiledCode(String name, File directory, boolean skipLoading) throws IOException {
    this("", name, directory, skipLoading);
  }

  public CompiledCode(String packageName, String name, File directory, boolean skipLoading)
      throws IOException {
    this.skipLoading = skipLoading;
    if (packageName != null && !packageName.trim().isEmpty()) {
      this.name = packageName + "." + name;
    } else {
      this.name = name;
    }
    String location = name;
    if (packageName != null && !packageName.trim().isEmpty()) {
      location = packageName.replaceAll("\\Q.\\E", "/") + "/" + name;
    }
    File classFile = new File(directory, location + Kind.CLASS.extension);
    this.byteCode = Files.readAllBytes(classFile.toPath());
  }

  public byte[] getByteCode() {
    return byteCode;
  }

  public boolean isSkipLoading() {
    return skipLoading;
  }

  public String getName() {
    return name;
  }
}
