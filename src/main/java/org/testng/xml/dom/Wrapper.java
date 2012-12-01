package org.testng.xml.dom;

import org.testng.collections.Lists;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.lang.annotation.Annotation;
import java.util.List;

public class Wrapper {

  private OnElement m_onElement;
  private OnElementList m_onElementList;
  private Tag m_tag;
  private TagContent m_tagContent;
  private Object m_bean;

  public Wrapper(Annotation a, Object bean) {
    m_bean = bean;
    if (a instanceof OnElement) m_onElement = (OnElement) a;
    else if (a instanceof OnElementList) m_onElementList = (OnElementList) a;
    else if (a instanceof Tag) m_tag = (Tag) a;
    else if (a instanceof TagContent) m_tagContent = (TagContent) a;
    else throw new RuntimeException("Illegal annotation " + a);
  }

  public String getTagName() {
    if (m_onElement != null) return m_onElement.tag();
    else if (m_onElementList != null) return m_onElementList.tag();
    else return m_tag.name();
  }

  public List<Object[]> getParameters(Element element) {
    List<Object[]> allParameters = Lists.newArrayList();
    if (m_onElement != null) {
      List<Object> result = Lists.newArrayList();
      for (String attributeName : m_onElement.attributes()) {
        result.add(element.getAttribute(attributeName));
      }
      allParameters.add(result.toArray());
    } else if (m_tag != null) {
      List<Object> result = Lists.newArrayList();
      result.add(m_bean);
      allParameters.add(result.toArray());
    } else {
      NodeList childNodes = element.getChildNodes();
      for (int i = 0; i < childNodes.getLength(); i++) {
        if (childNodes.item(i).hasAttributes()) {
          Element item = (Element) childNodes.item(i);
          List<Object> result = Lists.newArrayList();
          for (String attributeName : m_onElementList.attributes()) {
            result.add(item.getAttribute(attributeName));
          }
          allParameters.add(result.toArray());
        }
      }
    }

    return allParameters;
  }
}
