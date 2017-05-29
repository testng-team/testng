package org.testng.internal;

import org.testng.TestNG;
import org.testng.collections.Lists;
import org.testng.log4testng.Logger;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

/**
 * Utility class that finds all the classes in a given package.
 *
 * Created on Feb 24, 2006
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public class PackageUtils {
  private static final String UTF_8 = "UTF-8";
  private static final String PACKAGE_UTILS = PackageUtils.class.getSimpleName();
  private static String[] testClassPaths;

  /** The additional class loaders to find classes in. */
  private static final List<ClassLoader> classLoaders = Lists.newArrayList();

  private PackageUtils() {
    //Utility class. Defeat instantiation.
  }

  /** Add a class loader to the searchable loaders. */
  public static void addClassLoader(final ClassLoader loader) {
    classLoaders.add(loader);
  }

  /**
   *
   * @param pkgName - The package name
   * @param included - The inclusion list.
   * @param excluded - The exclusion list
   * @return - The list of all the classes inside this package
   * @throws IOException - if there is an exception.
   */
  public static String[] findClassesInPackage(String pkgName, List<String> included, List<String> excluded)
    throws IOException {
    String packageName = pkgName;
    String packageOnly = packageName;
    boolean recursive = false;
    if (packageName.endsWith(".*")) {
      packageOnly = packageName.substring(0, packageName.lastIndexOf(".*"));
      recursive = true;
    }

    List<String> vResult = Lists.newArrayList();
    String packageDirName = packageOnly.replace('.', '/') + (packageOnly.length() > 0 ? "/" : "");


    // go through additional class loaders
    List<ClassLoader> allClassLoaders = Lists.newArrayList();
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    if (contextClassLoader != null) {
      allClassLoaders.add(contextClassLoader);
    }
    allClassLoaders.addAll(classLoaders);

    List<URL> dirs = Lists.newArrayList();
    for (ClassLoader classLoader : allClassLoaders) {
      if (null == classLoader) {
        continue;
      }
      Enumeration<URL> dirEnumeration = classLoader.getResources(packageDirName);
      while(dirEnumeration.hasMoreElements()){
        URL dir = dirEnumeration.nextElement();
        dirs.add(dir);
      }
    }

    ParamContainer container = new ParamContainer(included,excluded,vResult);

    for (URL url : dirs) {
      packageName = processURL(packageName, container, packageOnly, recursive, packageDirName, url);
    }
    return vResult.toArray(new String[vResult.size()]);
  }

  private static String processURL(String pkgName, ParamContainer container,
      String packageOnly, boolean recursive , String packageDirName, URL url) throws IOException {
    String packageName = pkgName;
    List<String> included = container.getIncluded();
    List<String> excluded = container.getExcluded();
    List<String> vResult = container.getResult();

    String protocol = url.getProtocol();
    if (! matchTestClasspath(url, packageDirName, recursive)) {
      return packageName;
    }

    if ("file".equals(protocol)) {
      String path = URLDecoder.decode(url.getFile(), UTF_8);
      findClassesInDirPackage(packageOnly, included, excluded, path, recursive, vResult);
    } else if ("jar".equals(protocol)) {
      packageName = processJar(packageName, packageOnly, recursive, packageDirName, url, container);
    } else if ("bundleresource".equals(protocol)) {
      try {
        // BundleURLConnection
        URLConnection connection = url.openConnection();
        Class[] params = {};
        Method thisMethod = url.openConnection().getClass().getDeclaredMethod("getFileURL", params);
        Object[] paramsObj = {};
        URL fileUrl = (URL) thisMethod.invoke(connection, paramsObj);
        String path = URLDecoder.decode(fileUrl.getFile(), UTF_8);
        findClassesInDirPackage(packageOnly, included, excluded, path, recursive, vResult);
      } catch (Exception ex) {
        // ignore - probably not an Eclipse OSGi bundle
        Logger.getLogger(PackageUtils.class).debug(ex.getMessage(),ex);
      }
    }
    return packageName;
  }

  private static String processJar(String pkgName, String packageOnly, boolean recursive, String packageDirName,
      URL url, ParamContainer container) throws IOException {
    String packageName = pkgName;
    List<String> included = container.getIncluded();
    List<String> excluded = container.getExcluded();
    List<String> vResult = container.getResult();
    JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();
    Enumeration<JarEntry> entries = jar.entries();
    while (entries.hasMoreElements()) {
      JarEntry entry = entries.nextElement();
      String name = entry.getName();
      if (name.charAt(0) == '/') {
        name = name.substring(1);
      }
      if (! name.startsWith(packageDirName)) {
        continue;
      }
      int idx = name.lastIndexOf('/');
      if (idx != - 1) {
        packageName = name.substring(0, idx).replace('/', '.');
      }

      if (recursive || packageName.equals(packageOnly)) {
        //it's not inside a deeper dir
        Utils.log(PACKAGE_UTILS, 4, "Package name is " + packageName);
        if (name.endsWith(".class") && ! entry.isDirectory()) {
          String className = name.substring(packageName.length() + 1, name.length() - 6);
          Utils.log(PACKAGE_UTILS, 4, "Found class " + className + ", seeing it if it's included or excluded");
          includeOrExcludeClass(packageName, className, included, excluded, vResult);
        }
      }
    }
    return packageName;
  }

  private static String[] getTestClasspath() {
    if (null != testClassPaths) {
      return testClassPaths;
    }

    String testClasspath = System.getProperty(TestNG.TEST_CLASSPATH, "");
    if (testClasspath.trim().isEmpty()) {
      return new String[] {};
    }

    String[] classpathFragments= Utils.split(testClasspath, File.pathSeparator);
    testClassPaths = new String[classpathFragments.length];

    for(int i= 0; i < classpathFragments.length; i++)  {
      String path;
      if(classpathFragments[i].toLowerCase().endsWith(".jar") || classpathFragments[i].toLowerCase().endsWith(".zip")) {
        path= classpathFragments[i] + "!/";
      }
      else {
        if(classpathFragments[i].endsWith(File.separator)) {
          path= classpathFragments[i];
        }
        else {
          path= classpathFragments[i] + "/";
        }
      }

      testClassPaths[i]= path.replace('\\', '/');
    }

    return testClassPaths;
  }

  private static boolean matchTestClasspath(URL url, String lastFragment, boolean recursive) {
    String[] classpathFragments= getTestClasspath();
    if(classpathFragments.length ==0) {
      return true;
    }

    String fileName= "";
    try {
      fileName= URLDecoder.decode(url.getFile(), UTF_8);
    }
    catch(UnsupportedEncodingException ex) {
      Logger.getLogger(PackageUtils.class).debug(ex.getMessage(),ex);
      // ignore. should never happen
    }

    for(String classpathFrag: classpathFragments) {
      String path=  classpathFrag + lastFragment;
      int idx= fileName.indexOf(path);
      if((idx == -1) || (idx > 0 && fileName.charAt(idx-1) != '/')) {
        continue;
      }

      if(fileName.endsWith(classpathFrag + lastFragment)
          || (recursive && fileName.charAt(idx + path.length()) == '/')) {
        return true;
      }
    }

    return false;
  }

  private static void findClassesInDirPackage(String packageName,
                                              List<String> included,
                                              List<String> excluded,
                                              String packagePath,
                                              final boolean recursive,
                                              List<String> classes) {
    File dir = new File(packagePath);

    if (!dir.exists() || !dir.isDirectory()) {
      return;
    }

    File[] dirfiles = dir.listFiles(new FileFilter() {
          @Override
          public boolean accept(File file) {
            return (recursive && file.isDirectory())
              || (file.getName().endsWith(".class"))
              || (file.getName().endsWith(".groovy"));
          }
        });

    Utils.log(PACKAGE_UTILS, 4, "Looking for test classes in the directory: " + dir);
    if (dirfiles == null) {
      return;
    }
    for (File file : dirfiles) {
      if (file.isDirectory()) {
        findClassesInDirPackage(makeFullClassName(packageName, file.getName()),
                                included,
                                excluded,
                                file.getAbsolutePath(),
                                recursive,
                                classes);
      }
      else {
        String className = file.getName().substring(0, file.getName().lastIndexOf('.'));
        Utils.log(PACKAGE_UTILS, 4, "Found class " + className
            + ", seeing it if it's included or excluded");
        includeOrExcludeClass(packageName, className, included, excluded, classes);
      }
    }
  }

  private static String makeFullClassName(String pkg, String cls) {
    return pkg.length() > 0 ? pkg + "." + cls : cls;
  }

  private static void includeOrExcludeClass(String packageName, String className,
      List<String> included, List<String> excluded, List<String> classes)
  {
    if (isIncluded(packageName, included, excluded)) {
      Utils.log(PACKAGE_UTILS, 4, "... Including class " + className);
      classes.add(makeFullClassName(packageName, className));
    }
    else {
      Utils.log(PACKAGE_UTILS, 4, "... Excluding class " + className);
    }
  }

  /**
   * @return true if name should be included.
   */
  private static boolean isIncluded(String name,
      List<String> included, List<String> excluded)
  {
    boolean result;

    //
    // If no includes nor excludes were specified, return true.
    //
    if (included.isEmpty() && excluded.isEmpty()) {
      result = true;
    }
    else {
      boolean isIncluded = PackageUtils.find(name, included);
      boolean isExcluded = PackageUtils.find(name, excluded);
      if (isIncluded && !isExcluded) {
        result = true;
      }
      else if (isExcluded) {
        result = false;
      }
      else {
        result = included.isEmpty();
      }
    }
    return result;
  }

  private static boolean find(String name, List<String> list) {
    for (String regexpStr : list) {
      if (Pattern.matches(regexpStr, name)) {
        return true;
      }
    }
    return false;
  }

  /**
   * A POJO that is meant to only act as a container for parameters that are used inside of this
   * class (And to prevent method param count from going beyond 7)
   */
  private static class ParamContainer {
    private List<String> included;
    private List<String> excluded;
    private List<String> result;

    ParamContainer(List<String> included, List<String> excluded, List<String> result) {
      this.included = included;
      this.excluded = excluded;
      this.result = result;
    }

    public List<String> getExcluded() {
      return excluded;
    }

    public List<String> getIncluded() {
      return included;
    }

    public List<String> getResult() {
      return result;
    }
  }
}
