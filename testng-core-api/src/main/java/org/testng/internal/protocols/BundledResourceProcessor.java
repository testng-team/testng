package org.testng.internal.protocols;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.List;
import org.testng.collections.Lists;

class BundledResourceProcessor extends Processor {
  @Override
  public List<String> process(Input input, URL url) {
    return processBundledResources(
        url,
        input.getIncluded(),
        input.getExcluded(),
        input.getPackageWithoutWildCards(),
        input.isRecursive());
  }

  private static List<String> processBundledResources(
      URL url,
      List<String> included,
      List<String> excluded,
      String packageOnly,
      boolean recursive) {
    try {
      Class<?>[] params = {};
      // BundleURLConnection
      URLConnection connection = url.openConnection();
      Method thisMethod = url.openConnection().getClass().getDeclaredMethod("getFileURL", params);
      Object[] paramsObj = {};
      URL fileUrl = (URL) thisMethod.invoke(connection, paramsObj);
      return findClassesInDirPackage(
          packageOnly, included, excluded, URLDecoder.decode(fileUrl.getFile(), UTF_8), recursive);
    } catch (Exception ex) {
      // ignore - probably not an Eclipse OSGi bundle
    }
    return Lists.newArrayList();
  }
}
