package org.testng.xml.jaxb.mappers;

import java.util.List;
import java.util.stream.Collectors;
import org.testng.xml.XmlListener;
import org.testng.xml.jaxb.Listener;
import org.testng.xml.jaxb.Listeners;

public class ListenersMapper {

  public static List<XmlListener> toXmlListeners(Listeners listeners) {
    return listeners.getListener().stream()
        .map(
            l -> {
              XmlListener xmlListener = new XmlListener();
              xmlListener.setClassName(l.getClassName());
              return xmlListener;
            })
        .collect(Collectors.toList());
  }
}
