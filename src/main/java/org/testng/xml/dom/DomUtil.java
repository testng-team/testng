package org.testng.xml.dom;

import org.testng.xml.XmlSuite;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DomUtil {

  private XPath m_xpath;
  private Document m_document;

  public DomUtil(Document doc) {
    XPathFactory xpathFactory = XPathFactory.newInstance();
    m_xpath = xpathFactory.newXPath();
    m_document = doc;
  }

  public void populate(XmlSuite xmlSuite) throws XPathExpressionException {
    NodeList nodes = m_document.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      Node item = nodes.item(i);
      if ("suite".equals(item.getNodeName()) && item.getAttributes() != null) {
        populateAttributes(item, xmlSuite);
      }
    }

//    XPathExpression expr = m_xpath.compile("//suite/test");
//    NodeList tests = (NodeList) expr.evaluate(m_document, XPathConstants.NODESET);
//    for (int i = 0; i < tests.getLength(); i++) {
//      Node node = tests.item(i);
//      System.out.println("<test>:" + node);
//    }
  }

  private void populateAttributes(Node node, Object object) throws XPathExpressionException {
    for (int j = 0; j < node.getAttributes().getLength(); j++) {
      Node item = node.getAttributes().item(j);
      System.out.println(node.getAttributes().item(j));
      setProperty(object, item.getLocalName(), item.getNodeValue());
    }
  }

  private void setProperty(Object object, String name, Object value) {
    String methodName = toCamelCaseSetter(name);
    Method foundMethod = null;
    for (Method m : object.getClass().getDeclaredMethods()) {
      if (m.getName().equals(methodName)) {
        foundMethod = m;
        break;
      }
    }

    if (foundMethod == null) {
      p("Warning: couldn't find setter method " + methodName);
    } else {
      try {
        p("Invoking " + methodName + " with " + value);
        Class<?> type = foundMethod.getParameterTypes()[0];
        if (type == Boolean.class || type == boolean.class) {
          foundMethod.invoke(object, Boolean.parseBoolean(value.toString()));
        } else if (type == Integer.class || type == int.class) {
          foundMethod.invoke(object, Integer.parseInt(value.toString()));
        } else {
          foundMethod.invoke(object, value.toString());
        }
      } catch (IllegalArgumentException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      }
    }
  }

  private void p(String string) {
    System.out.println("[XPathUtil] " + string);
  }

  private String toCamelCaseSetter(String name) {
    StringBuilder result = new StringBuilder("set" + name.substring(0, 1).toUpperCase());
    for (int i = 1; i < name.length(); i++) {
      if (name.charAt(i) == '-') {
        result.append(Character.toUpperCase(name.charAt(i + 1)));
        i++;
      } else {
        result.append(name.charAt(i));
      }
    }
    return result.toString();
  }
}
