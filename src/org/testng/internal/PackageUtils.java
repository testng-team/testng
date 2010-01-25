package org.testng.internal;

import org.testng.TestNGCommandLineArgs;
import org.testng.collections.Lists;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.JarURLConnection;
import java.net.URL;
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
  private static String[] s_testClassPaths;
  
  /**
   *
   * @param packageName
   * @return The list of all the classes inside this package
   * @throws IOException
   */
  public static String[] findClassesInPackage(String packageName, 
      List<String> included, List<String> excluded)
    throws IOException 
  {
    String packageOnly = packageName;
    boolean recursive = false;
    if (packageName.endsWith(".*")) {
      packageOnly = packageName.substring(0, packageName.lastIndexOf(".*"));
      recursive = true;
    }
  
    List<String> vResult = Lists.newArrayList();
    String packageDirName = packageOnly.replace('.', '/');
    Enumeration<URL> dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
    while (dirs.hasMoreElements()) {
      URL url = dirs.nextElement();
      String protocol = url.getProtocol();
      if(!matchTestClasspath(url, packageDirName, recursive)) {
        continue;
      }
      
      if ("file".equals(protocol)) {
        findClassesInDirPackage(packageOnly, included, excluded,
                                URLDecoder.decode(url.getFile(), "UTF-8"),
                                recursive,
                                vResult);
      }
      else if ("jar".equals(protocol)) {
        JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
          JarEntry entry = entries.nextElement();
          String name = entry.getName();
          if (name.charAt(0) == '/') {
            name = name.substring(1);
          }
          if (name.startsWith(packageDirName)) {
            int idx = name.lastIndexOf('/');
            if (idx != -1) {
              packageName = name.substring(0, idx).replace('/', '.');
            }
  
            Utils.log("PackageUtils", 4, "Package name is " + packageName);
            if ((idx != -1) || recursive) {
              //it's not inside a deeper dir
              if (name.endsWith(".class") && !entry.isDirectory()) {
                String className = name.substring(packageName.length() + 1, name.length() - 6);
                Utils.log("PackageUtils", 4, "Found class " + className + ", seeing it if it's included or excluded");
                includeOrExcludeClass(packageName, className, included, excluded, vResult);
              }
            }
          }
        }
      }
    }
  
    String[] result = vResult.toArray(new String[vResult.size()]); 
    return result;
  }

  private static String[] getTestClasspath() {
    if(null != s_testClassPaths) {
      return s_testClassPaths;
    }
    
    String testClasspath= System.getProperty(TestNGCommandLineArgs.TEST_CLASSPATH);
    if(null == testClasspath) {
      return null;
    }
    
    String[] classpathFragments= Utils.split(testClasspath, File.pathSeparator);
    s_testClassPaths= new String[classpathFragments.length];
    
    for(int i= 0; i < classpathFragments.length; i++)  {
      String path= null;
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
      
      s_testClassPaths[i]= path.replace('\\', '/');
    }
    
    return s_testClassPaths;
  }
  
  /**
   * @param url
   * @return
   */
  private static boolean matchTestClasspath(URL url, String lastFragment, boolean recursive) {
    String[] classpathFragments= getTestClasspath();
    if(null == classpathFragments) {
      return true;
    }
    
    String protocol = url.getProtocol();
    String fileName= null;
    try {
      fileName= URLDecoder.decode(url.getFile(), "UTF-8");
    }
    catch(UnsupportedEncodingException ueex) {
      ; // ignore. should never happen
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
          public boolean accept(File file) {
            return (recursive && file.isDirectory())
              || (file.getName().endsWith(".class"))
              || (file.getName().endsWith(".groovy"));
          }
        });
  
    Utils.log("PackageUtils", 4, "Looking for test classes in the directory: " + dir);
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
        String className = file.getName().substring(0, file.getName().lastIndexOf("."));
        Utils.log("PackageUtils", 4, "Found class " + className
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
    if (isIncluded(className, included, excluded)) {
      Utils.log("PackageUtils", 4, "... Including class " + className);
      classes.add(makeFullClassName(packageName, className));
    }
    else {
      Utils.log("PackageUtils", 4, "... Excluding class " + className);
    }
  }

  /**
   * @return true if name should be included.
   */
  private static boolean isIncluded(String name, 
      List<String> included, List<String> excluded)
  {
    boolean result = false;
    
    //
    // If no includes nor excludes were specified, return true.
    //
    if (included.size() == 0 && excluded.size() == 0) {
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
        result = included.size() == 0;
      }
    }
    return result;
  }

  private static boolean find(String name, List<String> list) {
    for (String regexpStr : list) {
      if (Pattern.matches(regexpStr, name)) return true;
    }
    return false;
  }
}