package org.testng.xml.dom;

import org.w3c.dom.Node;

public interface ITagSetter<T> {
  void setProperty(String name, T parent, Node node);
}
