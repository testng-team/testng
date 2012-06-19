package org.testng.xml.dom;

import org.testng.collections.ListMultiMap;
import org.testng.collections.Lists;
import org.testng.xml.XmlSuite;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

public class XDom {
//  private static Map<String, Class<?>> m_map = Maps.newHashMap();
  private Document m_document;
  private ITagFactory m_tagFactory;

  public XDom(ITagFactory tagFactory, Document document)
      throws XPathExpressionException,
      InstantiationException, IllegalAccessException {
    m_tagFactory = tagFactory;
    m_document = document;
  }

  public Object parse() throws XPathExpressionException, InstantiationException,
      IllegalAccessException {
    Object result = null;
    NodeList nodes = m_document.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      Node item = nodes.item(i);
      if (item.getAttributes() != null) {
        String nodeName = item.getNodeName();
        System.out.println("Node name:" + nodeName);
        Class<?> c = m_tagFactory.getClassForTag(nodeName);
        if (c == null) {
          throw new RuntimeException("No class found for tag " + nodeName);
        }
  
        result = c.newInstance();
        if (ITagSetter.class.isAssignableFrom(result.getClass())) {
          System.out.println("Tag setter:"  + result);
        }
        populateAttributes(item, result);

        populateChildren(item, result);
      }
    }
    return result;
  }

  public void populateChildren(Node root, Object result) throws InstantiationException,
      IllegalAccessException, XPathExpressionException {
    NodeList childNodes = root.getChildNodes();
    ListMultiMap<String, Object> children = ListMultiMap.create();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node item = childNodes.item(i);
      if (item.getAttributes() != null) {
        String nodeName = item.getNodeName();
        boolean foundSetter = invokeOnSetter(result, nodeName, (Element) item);
        if (! foundSetter) {
          Class<?> c = m_tagFactory.getClassForTag(nodeName);
          if (c == null) {
            System.out.println("Warning: No class found for tag " + nodeName);
          } else {
            Object object = c.newInstance();
            if (ITagSetter.class.isAssignableFrom(object.getClass())) {
              System.out.println("Tag setter:"  + result);
              ((ITagSetter) object).setProperty(nodeName, result, item);
            } else {
              children.put(nodeName, object);
              populateAttributes(item, object);
            }
            populateChildren(item, object);
          }
        }
      }
    }
    System.out.println("Found children:" + children);
    for (String s : children.getKeys()) {
      setCollectionProperty(result, s, children.get(s));
    }
  }

  private boolean invokeOnSetter(Object result, String nodeName, Element element) {
    for (Method m : result.getClass().getMethods()) {
      OnElement onElement = m.getAnnotation(OnElement.class);
      if (onElement != null && nodeName.equals(onElement.tag())) {
        List<String> parameters = Lists.newArrayList();
        for (String attributeName : onElement.attributes()) {
          parameters.add(element.getAttribute(attributeName));
        }
        try {
          m.invoke(result, parameters.toArray());
          return true;
        } catch (IllegalArgumentException e) {
          e.printStackTrace();
        } catch (IllegalAccessException e) {
          e.printStackTrace();
        } catch (InvocationTargetException e) {
          e.printStackTrace();
        }
      }
    }
    return false;
  }

  private void populateAttributes(Node node, Object object) throws XPathExpressionException {
    for (int j = 0; j < node.getAttributes().getLength(); j++) {
      Node item = node.getAttributes().item(j);
//      p(node.getAttributes().item(j).toString());
      setProperty(object, item.getLocalName(), item.getNodeValue());
    }
  }

  private void setCollectionProperty(Object result, String property, List<Object> list) {
    Method method = findSetter(result, property + "s");
    if (method == null) {
      method = findSetter(result, property);
    }
    if (method != null) {
      try {
        if (Collection.class.isAssignableFrom(method.getParameterTypes()[0])) {
          method.invoke(result, list);
        } else {
          method.invoke(result, list.get(0));
        }
      } catch (IllegalArgumentException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      }
    } else {
      p("Warning: couldn't set property for " + property);
    }
  }

  private void setProperty(Object object, String name, Object value) {
    Method foundMethod = findSetter(object, name);

    if (foundMethod == null) {
      e("Couldn't find setter method for property" + name + " on " + object.getClass());
    } else {
      String methodName = foundMethod.getName();
      try {
//        p("Invoking " + methodName + " with " + value);
        Class<?> type = foundMethod.getParameterTypes()[0];
        if (type == Boolean.class || type == boolean.class) {
          foundMethod.invoke(object, Boolean.parseBoolean(value.toString()));
        } else if (type == Integer.class || type == int.class) {
          foundMethod.invoke(object, Integer.parseInt(value.toString()));
        } else {
          foundMethod.invoke(object, value);
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

  private Method findSetter(Object object, String name) {
    String methodName = toCamelCaseSetter(name);
    Method foundMethod = null;
    for (Method m : object.getClass().getDeclaredMethods()) {
      if (m.getName().equals(methodName)) {
        foundMethod = m;
        break;
      }
    }
    return foundMethod;
  }

  private String toCamelCaseSetter(String name) {
    return "set" + toCapitalizedCamelCase(name);
  }

  public static String toCapitalizedCamelCase(String name) {
    StringBuilder result = new StringBuilder(name.substring(0, 1).toUpperCase());
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

  private void p(String string) {
    System.out.println("[XDom] " + string);
  }

  private void e(String string) {
    System.out.println("[XDom] [Error] " + string);
  }

  public static class ChildSuite implements ITagSetter<XmlSuite> {
    @Override
    public void setProperty(String name, XmlSuite parent, Node node) {
      List<String> suiteFiles = Lists.newArrayList();
      for (int i = 0; i < node.getChildNodes().getLength(); i++) {
        Node item = node.getChildNodes().item(i);
        if (item.hasAttributes()) {
          System.out.println("found one");
          suiteFiles.add(((Element) item).getAttribute("path"));
        }
      }
      parent.setSuiteFiles(suiteFiles);
    }
  }

  public static class Parameter implements ITagSetter<XmlSuite> {
    @Override
    public void setProperty(String name, XmlSuite parent, Node node) {
      System.out.println("Filling parameter");
      Element element = (Element) node;
      parent.getParameters()
          .put(element.getAttribute("name"), element.getAttribute("value"));
    }
  }

  public static void main(String[] args) throws SAXException, IOException,
      ParserConfigurationException, XPathExpressionException, InstantiationException,
      IllegalAccessException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true); // never forget this!
    DocumentBuilder builder = factory.newDocumentBuilder();
    FileInputStream inputStream =
        new FileInputStream(new File("/Users/cedric/java/testng/src/test/resources/testng-all.xml"));
    Document doc = builder.parse(inputStream);
    XmlSuite result = (XmlSuite) new XDom(new TestNGTagFactory(), doc).parse();

    System.out.println(result.toXml());
  }
}
