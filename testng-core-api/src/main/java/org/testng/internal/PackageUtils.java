package org.testng.internal;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.jspecify.annotations.Nullable;
import org.testng.internal.protocols.Input;
import org.testng.internal.protocols.Processor;
import org.testng.internal.protocols.UnhandledIOException;

/**
 * Utility class that finds all the classes in a given package.
 *
 * <p>Created on Feb 24, 2006
 *
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public class PackageUtils {
  /**
   * The classpath fragments {@code testng.test.classpath} names, normalised once and cached.
   *
   * <p>Written by {@link #getTestClasspath()} without a lock, and read from every package scan --
   * which parallel suites run concurrently. {@code volatile} is what makes the array safe to
   * publish: without it a reader may see the reference while the element writes that filled it are
   * still invisible, and observe an array of nulls. That is not a crash but a silent wrong answer,
   * because {@code matchTestClasspath} would concatenate {@code "null"} into every comparison,
   * match nothing, and drop classes from the scan with no error anywhere. Two threads racing to
   * build it is harmless: the fragments derive from a system property, so both compute the same
   * value and either one may win.
   */
  private static volatile String @Nullable [] testClassPaths;

  /** The additional class loaders to find classes in. */
  private static final Collection<ClassLoader> classLoaders = new ConcurrentLinkedDeque<>();

  private PackageUtils() {
    // Utility class. Defeat instantiation.
  }

  /**
   * @param packageName - The package name
   * @param included - The inclusion list.
   * @param excluded - The exclusion list
   * @return - The list of all the classes inside this package
   * @throws IOException - if there is an exception.
   */
  public static String[] findClassesInPackage(
      String packageName, List<String> included, List<String> excluded) throws IOException {
    String packageNameWithoutWildCards = packageName;
    boolean recursive = packageName.endsWith(".*");
    if (recursive) {
      packageNameWithoutWildCards = packageName.substring(0, packageName.lastIndexOf(".*"));
    }

    String packageDirName =
        packageNameWithoutWildCards.replace('.', '/')
            + (packageNameWithoutWildCards.length() > 0 ? "/" : "");

    Input input =
        Input.Builder.newBuilder()
            .forPackageWithoutWildCards(packageNameWithoutWildCards)
            .withRecursive(recursive)
            .include(included)
            .exclude(excluded)
            .withPackageName(packageName)
            .forPackageDirectory(packageDirName)
            .build();

    // go through additional class loaders
    List<ClassLoader> allClassLoaders =
        ClassHelper.appendContextualClassLoaders(new ArrayList<>(classLoaders));

    return allClassLoaders.stream()
        .filter(Objects::nonNull)
        .flatMap(asURLs(packageDirName))
        .filter(url -> matchTestClasspath(url, packageDirName, recursive))
        .flatMap(url -> Processor.newInstance(url.getProtocol()).process(input, url).stream())
        .toArray(String[]::new);
  }

  private static String @Nullable [] getTestClasspath() {
    String[] cached = testClassPaths;
    if (null != cached) {
      return cached;
    }

    String testClasspath = RuntimeBehavior.getTestClasspath();
    if (null == testClasspath) {
      return null;
    }

    List<String> paths = new ArrayList<>();
    for (String fragment : Utils.split(testClasspath, File.pathSeparator)) {
      paths.add(toMatchedPath(fragment));
      // The system class loader builds the URL of a class path entry from its canonical path:
      // absolute, with no "." or "..", and with symbolic links resolved. So an entry such as
      // "./out" never matches as written. Match the canonical path of each entry too.
      String canonical = canonicalPath(fragment);
      if (canonical != null && !canonical.equals(fragment)) {
        paths.add(toMatchedPath(canonical));
      }
      // Before JDK 24, getCanonicalPath does not resolve symbolic links on Windows (JDK-8003887),
      // so an entry through a link matched nothing there. The real path resolves them everywhere.
      String real = realPath(fragment);
      if (real != null && !real.equals(fragment) && !real.equals(canonical)) {
        paths.add(toMatchedPath(real));
      }
    }

    String[] result = paths.toArray(new String[0]);
    testClassPaths = result;
    return result;
  }

  private static String toMatchedPath(String fragment) {
    String path;
    String lowerCase = fragment.toLowerCase(Locale.ROOT);
    if (lowerCase.endsWith(".jar") || lowerCase.endsWith(".zip")) {
      path = fragment + "!/";
    } else if (fragment.endsWith(File.separator)) {
      path = fragment;
    } else {
      path = fragment + "/";
    }
    return path.replace('\\', '/');
  }

  private static @Nullable String canonicalPath(String fragment) {
    if (fragment.isEmpty()) {
      return null;
    }
    try {
      return new File(fragment).getCanonicalPath();
    } catch (IOException e) {
      // The entry as written can still match.
      return null;
    }
  }

  private static @Nullable String realPath(String fragment) {
    if (fragment.isEmpty()) {
      return null;
    }
    try {
      return Path.of(fragment).toRealPath().toString();
    } catch (IOException | InvalidPathException e) {
      // An entry that does not exist has no real path. It can still match as written or through
      // its canonical path.
      return null;
    }
  }

  private static Function<ClassLoader, Stream<URL>> asURLs(String packageDir) {
    return cl -> {
      try {
        Iterator<URL> iterator = cl.getResources(packageDir).asIterator();
        return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false);
      } catch (IOException e) {
        throw new UnhandledIOException(e);
      }
    };
  }

  /**
   * Returns {@code true} when the URL of a package comes from an entry that {@code
   * testng.test.classpath} lists, or when the property is not set. The property filters the URLs
   * that the class loaders return. An entry that is not on the class path adds no classes.
   */
  private static boolean matchTestClasspath(URL url, String lastFragment, boolean recursive) {
    String[] classpathFragments = getTestClasspath();
    if (null == classpathFragments) {
      return true;
    }

    String fileName = URLDecoder.decode(url.getFile(), UTF_8);
    // The file part of a jar URL starts with the URL of the archive, as in
    // "file:/repo/tests.jar!/org/acme/". Without the scheme, an absolute entry matches it the same
    // way it matches a directory.
    if ("jar".equals(url.getProtocol()) && fileName.startsWith("file:")) {
      fileName = fileName.substring("file:".length());
    }

    for (String classpathFrag : classpathFragments) {
      String path = classpathFrag + lastFragment;
      int idx = fileName.indexOf(path);
      if ((idx == -1) || (idx > 0 && fileName.charAt(idx - 1) != '/')) {
        continue;
      }

      if (fileName.endsWith(classpathFrag + lastFragment)
          || (recursive && fileName.charAt(idx + path.length()) == '/')) {
        return true;
      }
    }
    return false;
  }
}
