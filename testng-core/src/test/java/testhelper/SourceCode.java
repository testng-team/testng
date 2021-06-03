package testhelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import javax.tools.JavaFileObject.Kind;

public class SourceCode {

  private final String name;
  private final File directory;
  private final boolean skipLoading;
  private final File location;

  public SourceCode(String name, String src, File directory, boolean skipLoading)
      throws IOException {
    this.name = name;
    this.directory = directory;
    this.skipLoading = skipLoading;
    this.location = new File(directory, name + Kind.SOURCE.extension);
    if (this.location.exists()) {
      this.location.delete();
    }
    Files.write(location.toPath(), src.getBytes(), StandardOpenOption.CREATE_NEW);
  }

  public File getDirectory() {
    return directory;
  }

  public String getName() {
    return name;
  }

  public boolean isSkipLoading() {
    return skipLoading;
  }

  public File getLocation() {
    return location;
  }
}
