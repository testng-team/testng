package org.testng.internal.protocols;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.regex.Pattern;
import org.testng.collections.Lists;
import org.testng.internal.Utils;

public abstract class Processor {

  protected static final String CLS_NAME = Processor.class.getSimpleName();

  public static Processor newInstance(String protocol) {
    Processor instance;
    switch (protocol.toLowerCase()) {
      case "file":
        instance = new FileProcessor();
        break;
      case "jar":
        instance = new JarProcessor();
        break;
      case "bundleresource":
        instance = new BundledResourceProcessor();
        break;
      default:
        instance = new NoOpProcessor();
    }
    return instance;
  }

  public abstract List<String> process(Input input, URL url);

  protected static List<String> findClassesInDirPackage(
      String packageName,
      List<String> included,
      List<String> excluded,
      String packagePath,
      final boolean recursive) {
    File dir = new File(packagePath);

    if (!dir.exists() || !dir.isDirectory()) {
      return Lists.newArrayList();
    }

    File[] dirfiles =
        dir.listFiles(
            file ->
                (recursive && file.isDirectory())
                    || (file.getName().endsWith(".class"))
                    || (file.getName().endsWith(".groovy")));

    Utils.log(CLS_NAME, 4, "Looking for test classes in the directory: " + dir);
    if (dirfiles == null) {
      return Lists.newArrayList();
    }
    List<String> classes = Lists.newArrayList();
    for (File file : dirfiles) {
      if (file.isDirectory()) {
        List<String> foundClasses =
            findClassesInDirPackage(
                makeFullClassName(packageName, file.getName()),
                included,
                excluded,
                file.getAbsolutePath(),
                recursive);
        classes.addAll(foundClasses);
      } else {
        String className = file.getName().substring(0, file.getName().lastIndexOf('.'));
        Utils.log(
            CLS_NAME, 4, "Found class " + className + ", seeing it if it's included or excluded");
        List<String> processedList =
            includeOrExcludeClass(packageName, className, included, excluded);
        classes.addAll(processedList);
      }
    }
    return classes;
  }

  private static String makeFullClassName(String pkg, String cls) {
    return pkg.length() > 0 ? pkg + "." + cls : cls;
  }

  protected static List<String> includeOrExcludeClass(
      String packageName, String className, List<String> included, List<String> excluded) {
    List<String> classes = Lists.newArrayList();
    if (isIncluded(packageName, included, excluded)) {
      Utils.log(CLS_NAME, 4, "... Including class " + className);
      classes.add(makeFullClassName(packageName, className));
    } else {
      Utils.log(CLS_NAME, 4, "... Excluding class " + className);
    }
    return classes;
  }

  /** @return true if name should be included. */
  private static boolean isIncluded(String name, List<String> included, List<String> excluded) {
    //
    // If no includes nor excludes were specified, return true.
    //
    if (included.isEmpty() && excluded.isEmpty()) {
      return true;
    }
    boolean isIncluded = find(name, included);
    boolean isExcluded = find(name, excluded);
    boolean result;
    if (isIncluded && !isExcluded) {
      result = true;
    } else if (isExcluded) {
      result = false;
    } else {
      result = included.isEmpty();
    }
    return result;
  }

  private static boolean find(String name, List<String> list) {
    return list.stream().parallel().anyMatch(each -> Pattern.matches(each, name));
  }
}
