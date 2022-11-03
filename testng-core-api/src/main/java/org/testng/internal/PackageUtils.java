package org.testng.internal;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.testng.collections.Lists;
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
  private static String[] testClassPaths;

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
        ClassHelper.appendContextualClassLoaders(Lists.newArrayList(classLoaders));

    return allClassLoaders.stream()
        .filter(Objects::nonNull)
        .flatMap(asURLs(packageDirName))
        .filter(url -> matchTestClasspath(url, packageDirName, recursive))
        .flatMap(url -> Processor.newInstance(url.getProtocol()).process(input, url).stream())
        .toArray(String[]::new);
  }

  private static String[] getTestClasspath() {
    if (null != testClassPaths) {
      return testClassPaths;
    }

    String testClasspath = RuntimeBehavior.getTestClasspath();
    if (null == testClasspath) {
      return null;
    }

    String[] classpathFragments = Utils.split(testClasspath, File.pathSeparator);
    testClassPaths = new String[classpathFragments.length];

    for (int i = 0; i < classpathFragments.length; i++) {
      String path;
      if (classpathFragments[i].toLowerCase().endsWith(".jar")
          || classpathFragments[i].toLowerCase().endsWith(".zip")) {
        path = classpathFragments[i] + "!/";
      } else {
        if (classpathFragments[i].endsWith(File.separator)) {
          path = classpathFragments[i];
        } else {
          path = classpathFragments[i] + "/";
        }
      }

      testClassPaths[i] = path.replace('\\', '/');
    }

    return testClassPaths;
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

  private static boolean matchTestClasspath(URL url, String lastFragment, boolean recursive) {
    String[] classpathFragments = getTestClasspath();
    if (null == classpathFragments) {
      return true;
    }

    String fileName = URLDecoder.decode(url.getFile(), UTF_8);

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
