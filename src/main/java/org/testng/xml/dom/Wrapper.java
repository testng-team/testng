package org.testng.xml.dom;

import org.testng.collections.Lists;
import org.w3c.dom.Element;

import java.lang.annotation.Annotation;
import java.util.List;

public class Wrapper {

  private OnElement m_onElement;
  private OnElementList m_onElementList;
  private Tag m_tag;

  public Wrapper(Annotation a) {
    if (a instanceof OnElement) m_onElement = (OnElement) a;
    else if (a instanceof OnElementList) m_onElementList = (OnElementList) a;
    else if (a instanceof Tag) m_tag = (Tag) a;
    else throw new RuntimeException("Illegal annotation " + a);
  }

  public String getTagName() {
    if (m_onElement != null) return m_onElement.tag();
    else if (m_onElementList != null) return m_onElementList.tag();
    else return m_tag.name();
  }

  public List<Object> getParameters(Element element) {
    List<Object> result = Lists.newArrayList();
    if (m_onElement != null) {
      for (String attributeName : m_onElement.attributes()) {
        result.add(element.getAttribute(attributeName));
      }
    } else if (m_tag != null) {
      result.add(element);
    } else {
      throw new RuntimeException("getParameters() problem");
    }

    return result;
  }
}
