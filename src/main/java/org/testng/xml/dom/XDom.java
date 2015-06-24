package org.testng.xml.dom;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.testng.Assert;
import org.testng.collections.ListMultiMap;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.internal.collections.Pair;
import org.testng.xml.XmlDefine;
import org.testng.xml.XmlGroups;
import org.testng.xml.XmlMethodSelector;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

  public Object parse() throws XPathExpressionException,
      InstantiationException, IllegalAccessException, SecurityException,
      IllegalArgumentException, NoSuchMethodException,
      InvocationTargetException {
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
      IllegalAccessException, XPathExpressionException, SecurityException, IllegalArgumentException, NoSuchMethodException, InvocationTargetException {
    p("populateChildren: " + root.getLocalName());
    NodeList childNodes = root.getChildNodes();
    ListMultiMap<String, Object> children = Maps.newListMultiMap();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node item = childNodes.item(i);
      if (item.getAttributes() != null) {
        String nodeName = item.getNodeName();
        if ("suite-files".equals(nodeName)) {
          System.out.println("BREAK");
        }

        Class<?> c = m_tagFactory.getClassForTag(nodeName);
        if (c == null) {
          System.out.println("Warning: No class found for tag " + nodeName);
          boolean foundSetter = invokeOnSetter(result, (Element) item, nodeName, null);
          System.out.println("  found setter:" + foundSetter);
        } else {
          Object object = instantiateElement(c, result);
          if (ITagSetter.class.isAssignableFrom(object.getClass())) {
            System.out.println("Tag setter:"  + result);
            ((ITagSetter) object).setProperty(nodeName, result, item);
          } else {
            children.put(nodeName, object);
            populateAttributes(item, object);
            populateContent(item, object);
          }
          boolean foundSetter = invokeOnSetter(result, (Element) item, nodeName, object);
//          setProperty(result, nodeName, object);
          populateChildren(item, object);
        }

//        boolean foundSetter = invokeOnSetter(result, (Element) item, nodeName);
//        if (! foundSetter) {
//          boolean foundListSetter = invokeOnListSetter(result, nodeName, item);
//          if (! foundListSetter) {
//          }
//        }
      }
    }
//    System.out.println("Found children:" + children);
//    for (String s : children.getKeys()) {
//      setCollectionProperty(result, s, children.get(s), object);
//    }
  }

  /**
   * Try to find a @ParentSetter. If this fails, try to find a constructor that takes the parent as a parameter.
   * If this fails, use the default constructor.
   */
  private Object instantiateElement(Class<?> c, Object parent)
      throws SecurityException, NoSuchMethodException,
      IllegalArgumentException, InstantiationException, IllegalAccessException,
      InvocationTargetException {
    Object result = null;
    Method m = findMethodAnnotatedWith(c, ParentSetter.class);
    if (m != null) {
        result = c.newInstance();
    	m.invoke(result, parent);
    } else {
	    try {
	      result = c.getConstructor(parent.getClass()).newInstance(parent);
	    } catch(NoSuchMethodException ex) {
	      result = c.newInstance();
	    }
    }

    return result;
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

  private Method findMethodAnnotatedWith(Class<?> c, Class<? extends Annotation> annotation) {
	  for (Method m : c.getMethods()) {
		  if (m.getAnnotation(annotation) != null) {
			  return m;
		  }
	  }
	  return null;
  }

private void populateContent(Node item, Object object) {
    for (int i = 0; i < item.getChildNodes().getLength(); i++) {
      Node child = item.getChildNodes().item(i);
      if (child instanceof Text) {
        setText(object, (Text) child);
      }
    }
  }

  private void setText(Object bean, Text child) {
    List<Pair<Method, Wrapper>> pairs =
        Reflect.findMethodsWithAnnotation(bean.getClass(), TagContent.class, bean);
    for (Pair<Method, Wrapper> pair : pairs) {
      try {
        pair.first().invoke(bean, child.getTextContent());
      } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException | DOMException e) {
        e.printStackTrace();
      }
    }
  }

  private boolean invokeOnSetter(Object object, Element element, String nodeName,
      Object bean) {
    Pair<Method, Wrapper> pair =
       Reflect.findSetterForTag(object.getClass(), nodeName, bean);

    List<Object[]> allParameters = null;
    if (pair != null) {
      Method m = pair.first();
      try {
        if (pair.second() != null) {
          allParameters = pair.second().getParameters(element);
        } else {
          allParameters = Lists.newArrayList();
          allParameters.add(new Object[] { bean });
        }

        for (Object[] p : allParameters) {
          m.invoke(object, p);
        }
        return true;
      } catch (IllegalArgumentException e) {
        System.out.println("Parameters: " + allParameters);
        e.printStackTrace();
      } catch (IllegalAccessException | InvocationTargetException e) {
        e.printStackTrace();
      }
    }

    return false;
  }

  private void populateAttributes(Node node, Object object) throws XPathExpressionException {
    for (int j = 0; j < node.getAttributes().getLength(); j++) {
      Node item = node.getAttributes().item(j);
      setProperty(object, item.getLocalName(), item.getNodeValue());
    }
  }

  private void setProperty(Object object, String name, Object value) {
    Pair<Method, Wrapper> setter = Reflect.findSetterForTag(object.getClass(), name,
        value);

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
      } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
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
      ParserConfigurationException, XPathExpressionException,
      InstantiationException, IllegalAccessException, SecurityException,
      IllegalArgumentException, NoSuchMethodException,
      InvocationTargetException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true); // never forget this!
    DocumentBuilder builder = factory.newDocumentBuilder();
    FileInputStream inputStream =
        new FileInputStream(new File(System.getProperty("user.home")
            + "/java/testng/src/test/resources/testng-all.xml"));
    Document doc = builder.parse(inputStream);
    XmlSuite result = (XmlSuite) new XDom(new TestNGTagFactory(), doc).parse();

    test(result);
    System.out.println(result.toXml());
  }

  private static void test(XmlSuite s) {
    Assert.assertEquals("TestNG", s.getName());
    Assert.assertEquals(s.getDataProviderThreadCount(), 3);
    Assert.assertEquals(s.getThreadCount(), 2);

    {
      // method-selectors
      List<XmlMethodSelector> selectors = s.getMethodSelectors();
      Assert.assertEquals(selectors.size(), 2);
      XmlMethodSelector s1 = selectors.get(0);
      Assert.assertEquals(s1.getLanguage(), "javascript");
      Assert.assertEquals(s1.getExpression(), "foo()");
      XmlMethodSelector s2 = selectors.get(1);
      Assert.assertEquals(s2.getClassName(), "SelectorClass");
      Assert.assertEquals(s2.getPriority(), 3);
    }

    {
      // child-suites
      List<String> suiteFiles = s.getSuiteFiles();
      Assert.assertEquals(suiteFiles, Arrays.asList("./junit-suite.xml"));
    }

    {
      // parameters
      Map<String, String> p = s.getParameters();
      Assert.assertEquals(p.size(), 2);
      Assert.assertEquals(p.get("suiteParameter"), "suiteParameterValue");
      Assert.assertEquals(p.get("first-name"), "Cedric");
    }

    {
      // run
      Assert.assertEquals(s.getIncludedGroups(), Arrays.asList("includeThisGroup"));
      Assert.assertEquals(s.getExcludedGroups(), Arrays.asList("excludeThisGroup"));
      XmlGroups groups = s.getGroups();

      // define
      List<XmlDefine> defines = groups.getDefines();
      Assert.assertEquals(defines.size(), 1);
      XmlDefine define = defines.get(0);
      Assert.assertEquals(define.getName(), "bigSuite");
      Assert.assertEquals(define.getIncludes(), Arrays.asList("suite1", "suite2"));

      // packages
      Assert.assertEquals(s.getPackageNames(), Arrays.asList("com.example1", "com.example2"));

      // listeners
      Assert.assertEquals(s.getListeners(),
          Arrays.asList("com.beust.Listener1", "com.beust.Listener2"));
      // dependencies
      // only defined on test for now
    }

    {
      // tests
      Assert.assertEquals(s.getTests().size(), 3);
      for (int i = 0; i < s.getTests().size(); i++) {
        if ("Nopackage".equals(s.getTests().get(i).getName())) {
          testNoPackage(s.getTests().get(i));
        }
      }
    }
  }

  private static void testNoPackage(XmlTest t) {
    Assert.assertEquals(t.getThreadCount(), 42);
    Assert.assertTrue(t.getAllowReturnValues());

    // define
    Map<String, List<String>> metaGroups = t.getMetaGroups();
    Assert.assertEquals(metaGroups.get("evenodd"), Arrays.asList("even", "odd"));

    // run
    Assert.assertEquals(t.getIncludedGroups(), Arrays.asList("nopackage", "includeThisGroup"));
    Assert.assertEquals(t.getExcludedGroups(), Arrays.asList("excludeThisGroup"));

    // dependencies
    Map<String, String> dg = t.getXmlDependencyGroups();
    Assert.assertEquals(dg.size(), 2);
    Assert.assertEquals(dg.get("e"), "f");
    Assert.assertEquals(dg.get("g"), "h");
  }
}
