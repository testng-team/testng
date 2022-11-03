package org.testng.internal.protocols;

import java.net.URL;
import java.util.Collections;
import java.util.List;

class NoOpProcessor extends Processor {
  @Override
  public List<String> process(Input input, URL url) {
    return Collections.emptyList();
  }
}
