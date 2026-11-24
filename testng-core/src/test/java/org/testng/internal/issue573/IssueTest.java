package org.testng.internal.issue573;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.internal.issue573.samples.PackagedSample;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  private static final int TIMEOUT_MINUTES = 2;
  private static final String ARCHIVE = "samples.jar";
  private static final String SAMPLES = PackagedSample.class.getPackage().getName();

  private Path classes;
  private Path workspace;
  private Path archive;
  private Path suite;
  // A symbolic link to the classes, or null where the platform does not let the test create one.
  private Path link;

  @BeforeClass
  public void prepare() throws Exception {
    classes =
        Paths.get(PackagedSample.class.getProtectionDomain().getCodeSource().getLocation().toURI());
    workspace = createDirInTempDir("issue573").toPath();
    archive = createArchive();
    suite = createSuite();
    link = createLink();
  }

  /**
   * Runs a suite with a {@code <package>} for each kind of {@code testng.test.classpath} entry.
   *
   * <p>TestNG reads the property once and keeps it in a static field. So each run happens in a
   * child Java virtual machine (JVM), with the entry and the working directory of the case.
   */
  @Test(description = "GITHUB-573", dataProvider = "entries")
  public void packageScanFindsTheSampleThroughTheEntry(String entry, File workingDirectory)
      throws Exception {
    File log = Files.createTempFile(workspace, "forked-output", ".log").toFile();

    int exitCode = run(entry, workingDirectory, log);
    String output = new String(Files.readAllBytes(log.toPath()), StandardCharsets.UTF_8);

    assertThat(exitCode)
        .as("the run with %s from %s%s", entry, workingDirectory, tail(output))
        .isZero();
  }

  @DataProvider
  public Object[][] entries() {
    File parent = classes.getParent().toFile();
    String directory = classes.getFileName().toString();
    String parentName = classes.getParent().getFileName().toString();
    File here = workspace.toFile();
    List<Object[]> entries = new ArrayList<>();
    // These entries matched before GITHUB-573, and still have to.
    entries.add(new Object[] {classes.toString(), here});
    entries.add(new Object[] {directory, parent});
    // Only the end of the path. The entry does not exist from the working directory.
    entries.add(new Object[] {parentName + "/" + directory, here});
    entries.add(new Object[] {ARCHIVE, here});
    // These entries matched nothing before GITHUB-573.
    entries.add(new Object[] {"./" + directory, parent});
    entries.add(new Object[] {"../" + parentName + "/" + directory, parent});
    entries.add(new Object[] {archive.toString(), here});
    entries.add(new Object[] {"./" + ARCHIVE, here});
    String otherCase = directory.toUpperCase(Locale.ROOT);
    if (Files.isDirectory(classes.resolveSibling(otherCase))) {
      // The file system ignores case, as macOS and Windows do by default.
      entries.add(new Object[] {"./" + otherCase, parent});
    }
    if (link != null) {
      entries.add(new Object[] {link.toString(), here});
    }
    return entries.toArray(new Object[0][]);
  }

  private int run(String entry, File workingDirectory, File log)
      throws IOException, InterruptedException {
    List<String> command = new ArrayList<>();
    command.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
    command.add("-Dtestng.test.classpath=" + entry);
    command.add("-cp");
    command.add(System.getProperty("java.class.path") + File.pathSeparator + archive);
    command.add(PackageScanLauncher.class.getName());
    command.add(suite.toString());
    command.add(Files.createTempDirectory(workspace, "test-output").toString());

    Process process =
        new ProcessBuilder(command)
            .directory(workingDirectory)
            .redirectErrorStream(true)
            .redirectOutput(log)
            .start();
    if (!process.waitFor(TIMEOUT_MINUTES, TimeUnit.MINUTES)) {
      process.destroyForcibly().waitFor();
      throw new AssertionError(
          "The forked run did not finish within " + TIMEOUT_MINUTES + " minutes");
    }
    return process.exitValue();
  }

  /** Copies the sample into an archive, which is the only class in it. */
  private Path createArchive() throws IOException {
    Path path = workspace.resolve(ARCHIVE);
    String packageDirectory = SAMPLES.replace('.', '/') + "/";
    String classFile = packageDirectory + PackagedSample.class.getSimpleName() + ".class";
    try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(path))) {
      // A class loader finds a package in an archive only through the entry of its directory.
      for (int end = packageDirectory.indexOf('/');
          end != -1;
          end = packageDirectory.indexOf('/', end + 1)) {
        jar.putNextEntry(new JarEntry(packageDirectory.substring(0, end + 1)));
        jar.closeEntry();
      }
      jar.putNextEntry(new JarEntry(classFile));
      Files.copy(classes.resolve(classFile), jar);
      jar.closeEntry();
    }
    return path;
  }

  private Path createLink() {
    try {
      return Files.createSymbolicLink(workspace.resolve("classes-link"), classes);
    } catch (IOException | UnsupportedOperationException e) {
      // Windows needs a privilege to create a symbolic link.
      return null;
    }
  }

  private Path createSuite() throws IOException {
    String xml =
        "<!DOCTYPE suite SYSTEM \"https://testng.org/testng-1.1.dtd\">\n"
            + "<suite name=\"GITHUB-573\">\n"
            + "  <test name=\"package scan\">\n"
            + "    <packages>\n"
            + "      <package name=\""
            + SAMPLES
            + "\"/>\n"
            + "    </packages>\n"
            + "  </test>\n"
            + "</suite>\n";
    Path path = workspace.resolve("testng.xml");
    Files.write(path, xml.getBytes(StandardCharsets.UTF_8));
    return path;
  }

  private static String tail(String output) {
    return System.lineSeparator() + "--- forked output ---" + System.lineSeparator() + output;
  }
}
