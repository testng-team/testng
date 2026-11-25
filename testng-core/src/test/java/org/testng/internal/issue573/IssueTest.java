package org.testng.internal.issue573;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.internal.ExitCode;
import org.testng.internal.issue573.samples.DirectorySample;
import org.testng.testhelper.CompiledCode;
import org.testng.testhelper.SimpleCompiler;
import org.testng.testhelper.SourceCode;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  private static final int TIMEOUT_MINUTES = 2;
  private static final String JAR = "samples.jar";
  private static final String ZIP = "samples.zip";
  private static final String NOT_ON_CLASS_PATH = "not-on-class-path";
  private static final String SAMPLES = DirectorySample.class.getPackage().getName();
  private static final String JAR_SAMPLE = SAMPLES + ".JarSample";
  private static final String ZIP_SAMPLE = SAMPLES + ".ZipSample";

  private Path classes;
  private Path workspace;
  private Path jar;
  private Path zip;
  private Path suite;
  // A symbolic link to the classes, or null where the platform does not let the test create one.
  private Path link;

  /**
   * Puts each sample class in one location only, so the test methods that ran show the locations
   * that the package scan read. {@code DirectorySample} is in the directory of the test classes.
   * The test compiles {@code JarSample} into a .jar archive, {@code ZipSample} into a .zip archive,
   * and {@code OffClassPathSample} into a directory that is not on the class path of the child.
   */
  @BeforeClass
  public void prepare() throws Exception {
    classes =
        Paths.get(
            DirectorySample.class.getProtectionDomain().getCodeSource().getLocation().toURI());
    workspace = createDirInTempDir("issue573").toPath();
    jar = createArchive(workspace.resolve(JAR), compile("JarSample", "generated"));
    zip = createArchive(workspace.resolve(ZIP), compile("ZipSample", "generated"));
    compile("OffClassPathSample", NOT_ON_CLASS_PATH);
    suite = createSuite();
    link = createLink();
  }

  /**
   * Runs a suite with a {@code <package>} for each kind of {@code testng.test.classpath} entry, and
   * checks the test methods that ran.
   *
   * <p>TestNG reads the property once and keeps it in a static field. So each run happens in a
   * child Java virtual machine (JVM), with the entry and the working directory of the case.
   */
  @Test(description = "GITHUB-573", dataProvider = "entries")
  public void packageScanRunsTheSamplesOfTheListedEntries(
      String entry, File workingDirectory, List<String> expected) throws Exception {
    Path results = Files.createTempFile(workspace, "results", ".txt");
    Path log = Files.createTempFile(workspace, "forked-output", ".log");

    int exitCode = run(entry, workingDirectory, results, log);
    String output = new String(Files.readAllBytes(log), StandardCharsets.UTF_8);
    String description =
        String.format("the run with %s from %s%s", entry, workingDirectory, tail(output));

    assertThat(Files.readAllLines(results, StandardCharsets.UTF_8))
        .as(description)
        .containsExactlyElementsOf(passed(expected));
    assertThat(exitCode).as(description).isEqualTo(expected.isEmpty() ? ExitCode.HAS_NO_TEST : 0);
  }

  @DataProvider
  public Object[][] entries() {
    File parent = classes.getParent().toFile();
    String directory = classes.getFileName().toString();
    String parentName = classes.getParent().getFileName().toString();
    File here = workspace.toFile();
    List<String> directorySample = Collections.singletonList(DirectorySample.class.getName());
    List<String> jarSample = Collections.singletonList(JAR_SAMPLE);
    List<String> zipSample = Collections.singletonList(ZIP_SAMPLE);
    List<Object[]> entries = new ArrayList<>();
    // Without the property, the package scan reads every location on the class path.
    entries.add(
        new Object[] {
          null, here, Arrays.asList(DirectorySample.class.getName(), JAR_SAMPLE, ZIP_SAMPLE)
        });
    // These entries matched before GITHUB-573, and still have to.
    entries.add(new Object[] {classes.toString(), here, directorySample});
    entries.add(new Object[] {directory, parent, directorySample});
    // Only the end of the path. The entry does not exist from the working directory.
    entries.add(new Object[] {parentName + "/" + directory, here, directorySample});
    entries.add(new Object[] {JAR, here, jarSample});
    entries.add(new Object[] {ZIP, here, zipSample});
    // The property filters the class path. The .zip archive is on the class path, but the
    // property does not list it.
    entries.add(
        new Object[] {
          parentName + "/" + directory + File.pathSeparator + JAR,
          here,
          Arrays.asList(DirectorySample.class.getName(), JAR_SAMPLE)
        });
    // Step 6 of GITHUB-573. The property does not add a location to the class path.
    entries.add(new Object[] {NOT_ON_CLASS_PATH, here, Collections.emptyList()});
    // These entries matched nothing before GITHUB-573.
    entries.add(new Object[] {"./" + directory, parent, directorySample});
    entries.add(new Object[] {"../" + parentName + "/" + directory, parent, directorySample});
    entries.add(new Object[] {jar.toString(), here, jarSample});
    entries.add(new Object[] {"./" + JAR, here, jarSample});
    entries.add(new Object[] {zip.toString(), here, zipSample});
    entries.add(new Object[] {"./" + ZIP, here, zipSample});
    String otherCase = directory.toUpperCase(Locale.ROOT);
    if (Files.isDirectory(classes.resolveSibling(otherCase))) {
      // The file system ignores case, as macOS and Windows do by default.
      entries.add(new Object[] {"./" + otherCase, parent, directorySample});
    }
    if (link != null) {
      entries.add(new Object[] {link.toString(), here, directorySample});
    }
    return entries.toArray(new Object[0][]);
  }

  private int run(String entry, File workingDirectory, Path results, Path log)
      throws IOException, InterruptedException {
    List<String> command = new ArrayList<>();
    command.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
    if (entry != null) {
      command.add("-Dtestng.test.classpath=" + entry);
    }
    command.add("-cp");
    command.add(
        System.getProperty("java.class.path")
            + File.pathSeparator
            + jar
            + File.pathSeparator
            + zip);
    command.add(PackageScanLauncher.class.getName());
    command.add(suite.toString());
    command.add(Files.createTempDirectory(workspace, "test-output").toString());
    command.add(results.toString());

    Process process =
        new ProcessBuilder(command)
            .directory(workingDirectory)
            .redirectErrorStream(true)
            .redirectOutput(log.toFile())
            .start();
    if (!process.waitFor(TIMEOUT_MINUTES, TimeUnit.MINUTES)) {
      process.destroyForcibly().waitFor();
      throw new AssertionError(
          "The forked run did not finish within " + TIMEOUT_MINUTES + " minutes");
    }
    return process.exitValue();
  }

  /** Compiles a sample class with one passing test method into a directory of the workspace. */
  private CompiledCode compile(String name, String directory) throws IOException {
    String source =
        "package "
            + SAMPLES
            + ";\n"
            + "public class "
            + name
            + " {\n"
            + "  @org.testng.annotations.Test\n"
            + "  public void passes() {}\n"
            + "}\n";
    File output = workspace.resolve(directory).toFile();
    return SimpleCompiler.compileSourceCode(new SourceCode(SAMPLES, name, source, output, true))
        .get(0);
  }

  /**
   * Writes an archive that holds the compiled sample, which is the only class in it. A .jar file
   * gets the jar format, and a .zip file the plain zip format.
   */
  private static Path createArchive(Path path, CompiledCode sample) throws IOException {
    String classFile = sample.getName().replace('.', '/') + ".class";
    try (OutputStream file = Files.newOutputStream(path);
        ZipOutputStream archive =
            path.toString().endsWith(".jar")
                ? new JarOutputStream(file)
                : new ZipOutputStream(file)) {
      // A class loader finds a package in an archive only through the entry of its directory.
      for (int end = classFile.indexOf('/'); end != -1; end = classFile.indexOf('/', end + 1)) {
        archive.putNextEntry(new ZipEntry(classFile.substring(0, end + 1)));
        archive.closeEntry();
      }
      archive.putNextEntry(new ZipEntry(classFile));
      archive.write(sample.getByteCode());
      archive.closeEntry();
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

  /** The lines that {@code PackageScanLauncher} writes when each sample class passes. */
  private static List<String> passed(List<String> samples) {
    List<String> lines = new ArrayList<>();
    for (String sample : samples) {
      lines.add(sample + ".passes PASSED");
    }
    Collections.sort(lines);
    return lines;
  }

  private static String tail(String output) {
    return System.lineSeparator() + "--- forked output ---" + System.lineSeparator() + output;
  }
}
