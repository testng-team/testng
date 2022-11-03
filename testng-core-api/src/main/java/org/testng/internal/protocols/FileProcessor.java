package org.testng.internal.protocols;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.URL;
import java.net.URLDecoder;
import java.util.List;

class FileProcessor extends Processor {

  @Override
  public List<String> process(Input input, URL url) {
    return findClassesInDirPackage(
        input.getPackageWithoutWildCards(),
        input.getIncluded(),
        input.getExcluded(),
        URLDecoder.decode(url.getFile(), UTF_8),
        input.isRecursive());
  }
}
