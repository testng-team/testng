package org.testng.testhelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.tools.JavaFileObject.Kind;

public class SourceCode {

  private final String packageName;
  private final String name;
  private final File directory;
  private final boolean skipLoading;
  private final File location;

  public SourceCode(String name, String src, File directory, boolean skipLoading)
      throws IOException {
    this("", name, src, directory, skipLoading);
  }

  public SourceCode(
      String packageName, String name, String src, File directory, boolean skipLoading)
      throws IOException {
    this.packageName = packageName;
    this.name = name;
    this.directory = directory;
    this.skipLoading = skipLoading;
    String path = name;
    boolean includesPackageName = false;
    if (packageName != null && !packageName.trim().isEmpty()) {
      path = packageName.replaceAll("\\Q.\\E", "/") + "/" + name;
      includesPackageName = true;
    }
    this.location = new File(directory, path + Kind.SOURCE.extension);
    if (!includesPackageName && this.location.exists()) {
      this.location.delete();
    }
    Path parentDir = this.location.getParentFile().toPath();
    if (!Files.exists(parentDir)) {
      Files.createDirectories(parentDir);
    }
    Files.write(location.toPath(), src.getBytes());
  }

  public File getDirectory() {
    return directory;
  }

  public String getName() {
    return name;
  }

  public boolean hasPackageName() {
    return packageName != null && !packageName.trim().isEmpty();
  }

  public String getPackageName() {
    return packageName;
  }

  public boolean isSkipLoading() {
    return skipLoading;
  }

  public File getLocation() {
    return location;
  }
}
