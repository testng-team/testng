package org.testng.xml.dom;

import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.log4testng.Logger;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DomUtil {

  private Document m_document;

  public DomUtil(Document doc) {
    m_document = doc;
  }

  public void populate(final XmlSuite xmlSuite) {
    NodeList nodes = m_document.getChildNodes();
    final Map<String, String> parameters = Maps.newHashMap();
    for (int i = 0; i < nodes.getLength(); i++) {
      Node item1 = nodes.item(i);

      Map<String, NodeProcessor> map = Maps.newHashMap();
      map.put(
          "parameter",
          node -> {
            Element e = (Element) node;
            parameters.put(e.getAttribute("name"), e.getAttribute("value"));
          });
      map.put(
          "test",
          node -> {
            XmlTest xmlTest = new XmlTest(xmlSuite);
            populateTest(xmlTest, node);
          });
      map.put(
          "suite-files",
          node -> {
            NodeList item2Children = node.getChildNodes();
            List<String> suiteFiles = Lists.newArrayList();
            for (int k = 0; k < item2Children.getLength(); k++) {
              Node item3 = item2Children.item(k);
              if (item3 instanceof Element) {
                Element e = (Element) item3;
                if ("suite-file".equals(item3.getNodeName())) {
                  suiteFiles.add(e.getAttribute("path"));
                }
              }
            }
            xmlSuite.setSuiteFiles(suiteFiles);
          });
      parseNodeAndChildren("suite", item1, xmlSuite, map);
    }

    xmlSuite.setParameters(parameters);
  }

  public interface NodeProcessor {
    void process(Node node);
  }

  private void parseNodeAndChildren(
      String name, Node root, Object object, Map<String, NodeProcessor> processors) {
    if (name.equals(root.getNodeName()) && root.getAttributes() != null) {
      populateAttributes(root, object);
      NodeList children = root.getChildNodes();
      for (int j = 0; j < children.getLength(); j++) {
        Node item2 = children.item(j);
        String nodeName = item2.getNodeName();
        NodeProcessor proc = processors.get(nodeName);
        if (proc != null) {
          proc.process(item2);
        } else if (!nodeName.startsWith("#")) {
          throw new RuntimeException("No processor found for " + nodeName);
        }
      }
    }
  }

  public static Iterator<Node> findChildren(Node node, String name) {
    List<Node> result = Lists.newArrayList();
    NodeList children = node.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node n = children.item(i);
      if (name.equals(n.getNodeName())) {
        result.add(n);
      }
    }
    return result.iterator();
  }

  private void populateTest(XmlTest xmlTest, Node item) {
    Map<String, String> testParameters = Maps.newHashMap();
    populateAttributes(item, xmlTest);
    NodeList itemChildren = item.getChildNodes();
    for (int k = 0; k < itemChildren.getLength(); k++) {
      Node item2 = itemChildren.item(k);
      if ("parameter".equals(item2.getNodeName())) {
        Element e = (Element) item2;
        testParameters.put(e.getAttribute("name"), e.getAttribute("value"));
      } else if ("classes".equals(item2.getNodeName())) {
        NodeList item2Children = item2.getChildNodes();
        for (int l = 0; l < item2Children.getLength(); l++) {
          Node item4 = item2Children.item(l);
          if ("class".equals(item4.getNodeName())) {
            XmlClass xmlClass = new XmlClass();
            populateAttributes(item4, xmlClass);
            xmlTest.getClasses().add(xmlClass);

            // TODO: excluded/included methods
          }
        }
      } else if ("groups".equals(item2.getNodeName())) {
        NodeList item2Children = item2.getChildNodes();
        List<String> includes = Lists.newArrayList();
        List<String> excludes = Lists.newArrayList();
        for (int l = 0; l < item2Children.getLength(); l++) {
          Node item3 = item2Children.item(l);
          if ("run".equals(item3.getNodeName())) {
            NodeList item3Children = item3.getChildNodes();
            for (int m = 0; m < item3Children.getLength(); m++) {
              Node item4 = item3Children.item(m);
              if ("include".equals(item4.getNodeName())) {
                includes.add(((Element) item4).getAttribute("name"));
              } else if ("exclude".equals(item4.getNodeName())) {
                excludes.add(((Element) item4).getAttribute("name"));
              }
            }
          } else if ("dependencies".equals(item3.getNodeName())) {
            NodeList item3Children = item3.getChildNodes();
            for (int m = 0; m < item3Children.getLength(); m++) {
              Node item4 = item3Children.item(m);
              if ("group".equals(item4.getNodeName())) {
                Element e = (Element) item4;
                xmlTest.addXmlDependencyGroup(e.getAttribute("name"), e.getAttribute("depends-on"));
              }
            }
          } else if ("define".equals(item3.getNodeName())) {
            xmlDefine(xmlTest, item3);
          }
        }
        xmlTest.setIncludedGroups(includes);
        xmlTest.setExcludedGroups(excludes);
      } // TODO: (method-selectors?,packages?) >
    }

    xmlTest.setParameters(testParameters);
  }

  /** Parse the <define> tag. */
  private void xmlDefine(XmlTest xmlTest, Node item) {
    NodeList item3Children = item.getChildNodes();
    List<String> groups = Lists.newArrayList();
    for (int m = 0; m < item3Children.getLength(); m++) {
      Node item4 = item3Children.item(m);
      if ("include".equals(item4.getNodeName())) {
        Element e = (Element) item4;
        groups.add(e.getAttribute("name"));
      }
    }
    xmlTest.addMetaGroup(((Element) item).getAttribute("name"), groups);
  }

  private void populateAttributes(Node node, Object object) {
    for (int j = 0; j < node.getAttributes().getLength(); j++) {
      Node item = node.getAttributes().item(j);
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

    if (foundMethod != null) {
      try {
        Class<?> type = foundMethod.getParameterTypes()[0];
        if (type == Boolean.class || type == boolean.class) {
          foundMethod.invoke(object, Boolean.parseBoolean(value.toString()));
        } else if (type == Integer.class || type == int.class) {
          foundMethod.invoke(object, Integer.parseInt(value.toString()));
        } else {
          foundMethod.invoke(object, value.toString());
        }
      } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
        Logger.getLogger(DomUtil.class).error(e.getMessage(), e);
      }
    }
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
