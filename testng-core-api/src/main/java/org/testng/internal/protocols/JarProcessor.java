package org.testng.internal.protocols;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.testng.collections.Lists;
import org.testng.internal.Utils;

class JarProcessor extends Processor {
  @Override
  public List<String> process(Input input, URL url) {
    try {
      return processJar(
          url,
          input.getIncluded(),
          input.getExcluded(),
          input.getPackageWithoutWildCards(),
          input.isRecursive(),
          input.getPackageDirName(),
          input.getPackageName());
    } catch (IOException e) {
      throw new UnhandledIOException(e);
    }
  }

  private static List<String> processJar(
      URL url,
      List<String> included,
      List<String> excluded,
      String packageOnly,
      boolean recursive,
      String packageDirName,
      String packageName)
      throws IOException {
    List<String> vResult = Lists.newArrayList();
    JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();
    Enumeration<JarEntry> entries = jar.entries();
    while (entries.hasMoreElements()) {
      JarEntry entry = entries.nextElement();
      String name = entry.getName();
      if (name.startsWith("module-info") || name.startsWith("META-INF")) {
        continue;
      }
      if (name.charAt(0) == '/') {
        name = name.substring(1);
      }
      if (name.startsWith(packageDirName)) {
        int idx = name.lastIndexOf('/');
        if (idx != -1) {
          packageName = name.substring(0, idx).replace('/', '.');
        }

        if (recursive || packageName.equals(packageOnly)) {
          // it's not inside a deeper dir
          Utils.log(CLS_NAME, 4, "Package name is " + packageName);
          if (name.endsWith(".class") && !entry.isDirectory()) {
            String className = name.substring(packageName.length() + 1, name.length() - 6);
            Utils.log(
                CLS_NAME,
                4,
                "Found class " + className + ", seeing it if it's included or excluded");
            List<String> processedList =
                includeOrExcludeClass(packageName, className, included, excluded);
            vResult.addAll(processedList);
          }
        }
      }
    }
    return vResult;
  }
}
