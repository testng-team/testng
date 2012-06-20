package org.testng.xml.dom;

import org.testng.Assert;
import org.testng.collections.ListMultiMap;
import org.testng.collections.Lists;
import org.testng.internal.collections.Pair;
import org.testng.xml.XmlDefine;
import org.testng.xml.XmlGroups;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
        populateAttributes(item, result);
        if (ITagSetter.class.isAssignableFrom(result.getClass())) {
          throw new RuntimeException("TAG SETTER");
        }
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
        if ("suite-files".equals(nodeName)) {
          System.out.println("BREAK");
        }

        boolean foundSetter = invokeOnSetter(result, (Element) item, nodeName);
        if (! foundSetter) {
          boolean foundListSetter = invokeOnListSetter(result, nodeName, item);
          if (! foundListSetter) {
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
              setProperty(result, nodeName, object);
              populateChildren(item, object);
            }
          }
        }
      }
    }
//    System.out.println("Found children:" + children);
    for (String s : children.getKeys()) {
      setCollectionProperty(result, s, children.get(s));
    }
  }

//  private List<Pair<Method, ? extends Annotation>>
//      findMethodsWithAnnotation(Class<?> c, Class<? extends Annotation> ac) {
//    List<Pair<Method, ? extends Annotation>> result = Lists.newArrayList();
//    for (Method m : c.getMethods()) {
//      Annotation a = m.getAnnotation(ac);
//      if (a != null) {
//        result.add(Pair.of(m, a));
//      }
//    }
//    return result;
//  }

  private boolean invokeOnSetter(Object object, Element element, String nodeName) {
    Pair<Method, Wrapper> pair =
        Reflect.findSetterForTag(object.getClass(), nodeName);

    if (pair != null) {
      Method m = pair.first();
//      OnElement onElement = (OnElement) pair.second();
//      List<String> parameters = Lists.newArrayList();
//      for (String attributeName : onElement.attributes()) {
//        parameters.add(element.getAttribute(attributeName));
//      }
      try {
        m.invoke(object, pair.second().getParameters(element));
        return true;
      } catch (IllegalArgumentException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      }
    }

    return false;
  }

  private boolean invokeOnListSetter(Object object, String tagName, Node node) {
    Pair<Method, Wrapper> pair =
        Reflect.findSetterForTag(object.getClass(), tagName);

    if (pair != null) {
      OnElementList onElement = (OnElementList) pair.second();
      if (onElement != null && tagName.equals(onElement.tag())) {
        List<List<Object>> parameterList = Lists.newArrayList();
        String[] attributeNames = onElement.attributes();
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
          Node item = node.getChildNodes().item(i);
          if (item.hasAttributes()) {
            List<Object> l = Lists.newArrayList();
            for (int j = 0; j < attributeNames.length; j++) {
              l.add(((Element) item).getAttribute(attributeNames[j]));
            }
            parameterList.add(l);
          }
        }
        try {
//          System.out.println("Invoking");
          for (List<Object> parameters : parameterList) {
            pair.first().invoke(object, parameters.toArray());
          }
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

  private void setCollectionProperty(Object object, String property, List<Object> list) {
    Pair<Method, Wrapper> setter = Reflect.findSetterForTag(object.getClass(),
        property + "s");
    if (setter == null) {
      setter = Reflect.findSetterForTag(object.getClass(), property);
    }

    if (setter != null) {
      Method method = setter.first();
      try {
        if (Collection.class.isAssignableFrom(method.getParameterTypes()[0])) {
          method.invoke(object, list);
        } else {
          method.invoke(object, list.get(0));
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

//  private Method findMethodTaggedWith(Class<?> c, String tagName) {
//    Pair<Method, ? extends Annotation> pair = findMethodWithAnnotation(c, Tag.class);
//    if (pair != null && ((Tag) pair.second()).name().equals(tagName)) {
//      return pair.first();
//    } else {
//      return null;
//    }
//  }

  private void setProperty(Object object, String name, Object value) {
    Pair<Method, Wrapper> setter = Reflect.findSetterForTag(object.getClass(), name);

    if (setter != null) {
      Method foundMethod = setter.first();
      try {
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
    } else {
      e("Couldn't find setter method for property" + name + " on " + object.getClass());
    }
  }

//  private Method findSetter(Object object, String name) {
//    String methodName = toCamelCaseSetter(name);
//    Method foundMethod = null;
//    for (Method m : object.getClass().getDeclaredMethods()) {
//      if (m.getName().equals(methodName)) {
//        foundMethod = m;
//        break;
//      }
//    }
//    return foundMethod;
//  }

  private void p(String string) {
    System.out.println("[XDom] " + string);
  }

  private void e(String string) {
    System.out.println("[XDom] [Error] " + string);
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

    test(result);
    System.out.println(result.toXml());
  }

  private static void test(XmlSuite s) {
    Assert.assertEquals("TestNG", s.getName());
    Assert.assertEquals(3, s.getDataProviderThreadCount());
    Assert.assertEquals(2, s.getThreadCount());

    {
      // child-suites
      List<String> suiteFiles = s.getSuiteFiles();
      Assert.assertEquals(Arrays.asList("./junit-suite.xml"), suiteFiles);
    }

    {
      // parameters
      Map<String, String> p = s.getParameters();
      Assert.assertEquals(2, p.size());
      Assert.assertEquals("suiteParameterValue", p.get("suiteParameter"));
      Assert.assertEquals("Cedric", p.get("first-name"));
    }

    {
      // run
      Assert.assertEquals(Arrays.asList("includeThisGroup"), s.getIncludedGroups());
      Assert.assertEquals(Arrays.asList("excludeThisGroup"), s.getExcludedGroups());
      XmlGroups groups = s.getGroups();

      // define
      List<XmlDefine> defines = groups.getDefines();
      Assert.assertEquals(1, defines.size());
      XmlDefine define = defines.get(0);
      Assert.assertEquals("bigSuite", define.getName());
      Assert.assertEquals(Arrays.asList("suite1", "suite2"), define.getIncludes());

      // packages
      Assert.assertEquals(Arrays.asList("com.example1", "com.example2"), s.getPackageNames());

      // listeners
      Assert.assertEquals(Arrays.asList("com.beust.Listener1", "com.beust.Listener2"),
          s.getListeners());
      // dependencies
      // only defined on test for now
    }

    {
      // tests
      Assert.assertEquals(3, s.getTests().size());
    }
  }
}
