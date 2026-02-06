package org.testng.xml.jaxb.mappers;

import java.util.List;
import java.util.stream.Collectors;
import org.testng.xml.jaxb.Listener;
import org.testng.xml.jaxb.Listeners;

public class ListenersMapper {

  public static List<String> toXmlListeners(Listeners listeners) {
    return listeners.getListener().stream()
        .map(Listener::getClassName)
        .collect(Collectors.toList());
  }
}
